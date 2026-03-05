package com.revy.mvpbanking.admin.application;

import com.revy.mvpbanking.account.domain.AccountStatus;
import com.revy.mvpbanking.approval.domain.ApprovalStatus;
import com.revy.mvpbanking.customer.domain.CustomerStatus;
import com.revy.mvpbanking.exchange.domain.ExchangeRequestStatus;
import com.revy.mvpbanking.fx.domain.FxRate;
import com.revy.mvpbanking.funding.domain.FundingRequestStatus;
import com.revy.mvpbanking.stock.domain.StockOrder;
import com.revy.mvpbanking.stock.domain.StockOrderStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
class AdminOverviewMetricsCalculator {

    private static final Duration APPROVAL_SLA_THRESHOLD = Duration.ofHours(2);
    private static final Duration MARKET_DATA_FRESHNESS_THRESHOLD = Duration.ofHours(12);

    AdminOverviewMetrics calculate(AdminOverviewDataset dataset, Instant now) {
        long pendingApprovals = dataset.approvals().stream()
                .filter(item -> item.getStatus() == ApprovalStatus.PENDING)
                .count();
        long overdueApprovals = dataset.approvals().stream()
                .filter(item -> item.getStatus() == ApprovalStatus.PENDING)
                .filter(item -> item.getCreatedAt() != null && item.getCreatedAt().isBefore(now.minus(APPROVAL_SLA_THRESHOLD)))
                .count();
        long reviewRequiredCustomers = dataset.customers().stream()
                .filter(item -> item.getStatus() == CustomerStatus.REVIEW_REQUIRED)
                .count();
        long lockedAccounts = dataset.accounts().stream()
                .filter(item -> item.getStatus() == AccountStatus.LOCKED)
                .count();
        long pendingFundingRequests = dataset.fundingRequests().stream()
                .filter(item -> item.getStatus() == FundingRequestStatus.PENDING_APPROVAL)
                .count();
        long pendingExchanges = dataset.exchangeRequests().stream()
                .filter(item -> item.getStatus() == ExchangeRequestStatus.PENDING_APPROVAL)
                .count();
        long pendingStockOrders = dataset.stockOrders().stream()
                .filter(item -> item.getStatus() == StockOrderStatus.PENDING_APPROVAL)
                .count();
        long partiallyFilledOrders = dataset.stockOrders().stream()
                .filter(item -> item.getStatus() == StockOrderStatus.PARTIALLY_FILLED)
                .count();

        long staleFxPairs = dataset.latestFxRates().stream()
                .filter(item -> item.getEffectiveAt().isBefore(now.minus(MARKET_DATA_FRESHNESS_THRESHOLD)))
                .count();
        long staleStockQuotes = dataset.latestStockQuotes().stream()
                .filter(item -> item.getEffectiveAt().isBefore(now.minus(MARKET_DATA_FRESHNESS_THRESHOLD)))
                .count();

        BigDecimal pendingInstructionVolumeKrw = pendingInstructionVolumeKrw(
                dataset.fundingRequests(),
                dataset.exchangeRequests(),
                dataset.stockOrders(),
                dataset.latestFxRates()
        );

        return new AdminOverviewMetrics(
                pendingApprovals,
                overdueApprovals,
                reviewRequiredCustomers,
                lockedAccounts,
                pendingFundingRequests,
                pendingExchanges,
                pendingStockOrders,
                partiallyFilledOrders,
                pendingInstructionVolumeKrw,
                dataset.latestFxRates().stream().map(FxRate::getEffectiveAt).max(Instant::compareTo).orElse(null),
                dataset.latestStockQuotes().stream().map(item -> item.getEffectiveAt()).max(Instant::compareTo).orElse(null),
                dataset.latestFxRates().size() - staleFxPairs,
                staleFxPairs,
                dataset.latestStockQuotes().size() - staleStockQuotes,
                staleStockQuotes
        );
    }

    private BigDecimal pendingInstructionVolumeKrw(
            List<com.revy.mvpbanking.funding.domain.FundingRequest> fundingRequests,
            List<com.revy.mvpbanking.exchange.domain.ExchangeRequest> exchangeRequests,
            List<StockOrder> stockOrders,
            List<FxRate> latestFxRates
    ) {
        BigDecimal pendingFundingVolume = fundingRequests.stream()
                .filter(item -> item.getStatus() == FundingRequestStatus.PENDING_APPROVAL)
                .map(item -> convertToKrw(item.getAmount(), item.getCurrency(), latestFxRates))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingExchangeVolume = exchangeRequests.stream()
                .filter(item -> item.getStatus() == ExchangeRequestStatus.PENDING_APPROVAL)
                .map(item -> convertToKrw(item.getFromAmount(), item.getFromCurrency(), latestFxRates))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingOrderVolume = stockOrders.stream()
                .filter(item -> item.getStatus() == StockOrderStatus.PENDING_APPROVAL || item.getStatus() == StockOrderStatus.PARTIALLY_FILLED)
                .map(item -> {
                    BigDecimal remainingQuantity = item.getRemainingQuantity() == null ? item.getQuantity() : item.getRemainingQuantity();
                    BigDecimal remainingNotional = remainingQuantity.multiply(item.getLimitPrice()).setScale(4, RoundingMode.HALF_UP);
                    return convertToKrw(remainingNotional, item.getCurrency(), latestFxRates);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return pendingFundingVolume.add(pendingExchangeVolume).add(pendingOrderVolume).setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal convertToKrw(BigDecimal amount, String currency, List<FxRate> latestFxRates) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        if ("KRW".equalsIgnoreCase(currency)) {
            return amount.setScale(4, RoundingMode.HALF_UP);
        }

        for (FxRate fxRate : latestFxRates) {
            if (fxRate.getBaseCurrency().equalsIgnoreCase(currency) && fxRate.getQuoteCurrency().equalsIgnoreCase("KRW")) {
                return amount.multiply(fxRate.getRate()).setScale(4, RoundingMode.HALF_UP);
            }
            if (fxRate.getBaseCurrency().equalsIgnoreCase("KRW")
                    && fxRate.getQuoteCurrency().equalsIgnoreCase(currency)
                    && fxRate.getRate().signum() > 0) {
                return amount.divide(fxRate.getRate(), 4, RoundingMode.HALF_UP);
            }
        }

        return amount.setScale(4, RoundingMode.HALF_UP);
    }
}
