package com.revy.mvpbanking.admin.application;

import com.revy.mvpbanking.account.domain.Account;
import com.revy.mvpbanking.account.domain.AccountRepository;
import com.revy.mvpbanking.account.domain.AccountStatus;
import com.revy.mvpbanking.approval.domain.ApprovalRequest;
import com.revy.mvpbanking.approval.domain.ApprovalRequestRepository;
import com.revy.mvpbanking.approval.domain.ApprovalStatus;
import com.revy.mvpbanking.audit.application.AuditLogService;
import com.revy.mvpbanking.audit.domain.AuditActionType;
import com.revy.mvpbanking.customer.domain.Customer;
import com.revy.mvpbanking.customer.domain.CustomerRepository;
import com.revy.mvpbanking.customer.domain.CustomerStatus;
import com.revy.mvpbanking.exchange.domain.ExchangeRequest;
import com.revy.mvpbanking.exchange.domain.ExchangeRequestRepository;
import com.revy.mvpbanking.exchange.domain.ExchangeRequestStatus;
import com.revy.mvpbanking.fx.domain.FxRate;
import com.revy.mvpbanking.fx.domain.FxRateRepository;
import com.revy.mvpbanking.funding.domain.FundingRequest;
import com.revy.mvpbanking.funding.domain.FundingRequestRepository;
import com.revy.mvpbanking.funding.domain.FundingRequestStatus;
import com.revy.mvpbanking.admin.presentation.AdminOverviewResponse;
import com.revy.mvpbanking.stock.domain.StockOrder;
import com.revy.mvpbanking.stock.domain.StockOrderRepository;
import com.revy.mvpbanking.stock.domain.StockOrderStatus;
import com.revy.mvpbanking.stock.domain.StockQuote;
import com.revy.mvpbanking.stock.domain.StockQuoteRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminOverviewService {

    private static final Duration APPROVAL_SLA_THRESHOLD = Duration.ofHours(2);
    private static final Duration MARKET_DATA_FRESHNESS_THRESHOLD = Duration.ofHours(12);

    private final ApprovalRequestRepository approvalRequestRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final FundingRequestRepository fundingRequestRepository;
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final StockOrderRepository stockOrderRepository;
    private final FxRateRepository fxRateRepository;
    private final StockQuoteRepository stockQuoteRepository;
    private final AuditLogService auditLogService;

    public AdminOverviewService(
            ApprovalRequestRepository approvalRequestRepository,
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            FundingRequestRepository fundingRequestRepository,
            ExchangeRequestRepository exchangeRequestRepository,
            StockOrderRepository stockOrderRepository,
            FxRateRepository fxRateRepository,
            StockQuoteRepository stockQuoteRepository,
            AuditLogService auditLogService
    ) {
        this.approvalRequestRepository = approvalRequestRepository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.fundingRequestRepository = fundingRequestRepository;
        this.exchangeRequestRepository = exchangeRequestRepository;
        this.stockOrderRepository = stockOrderRepository;
        this.fxRateRepository = fxRateRepository;
        this.stockQuoteRepository = stockQuoteRepository;
        this.auditLogService = auditLogService;
    }

    public AdminOverviewResponse getOverview() {
        auditLogService.logCurrentActor(AuditActionType.ADMIN_OVERVIEW_VIEWED, "ADMIN_OVERVIEW", "dashboard", "Viewed admin overview");

        List<ApprovalRequest> approvals = approvalRequestRepository.findAllByOrderByCreatedAtDesc();
        List<Customer> customers = customerRepository.findAllByOrderByCreatedAtDesc();
        List<Account> accounts = accountRepository.findAllByOrderByCreatedAtDesc();
        List<FundingRequest> fundingRequests = fundingRequestRepository.findAllByOrderByCreatedAtDesc();
        List<ExchangeRequest> exchangeRequests = exchangeRequestRepository.findAllByOrderByCreatedAtDesc();
        List<StockOrder> stockOrders = stockOrderRepository.findAllByOrderByCreatedAtDesc();
        List<FxRate> latestFxRates = latestFxRates(fxRateRepository.findAllByOrderByEffectiveAtDesc());
        List<StockQuote> latestStockQuotes = latestStockQuotes(stockQuoteRepository.findAllByOrderByEffectiveAtDesc());

        Instant now = Instant.now();
        long pendingApprovals = approvals.stream().filter(item -> item.getStatus() == ApprovalStatus.PENDING).count();
        long overdueApprovals = approvals.stream()
                .filter(item -> item.getStatus() == ApprovalStatus.PENDING)
                .filter(item -> item.getCreatedAt() != null && item.getCreatedAt().isBefore(now.minus(APPROVAL_SLA_THRESHOLD)))
                .count();
        long reviewRequiredCustomers = customers.stream().filter(item -> item.getStatus() == CustomerStatus.REVIEW_REQUIRED).count();
        long lockedAccounts = accounts.stream().filter(item -> item.getStatus() == AccountStatus.LOCKED).count();
        long pendingFundingRequests = fundingRequests.stream().filter(item -> item.getStatus() == FundingRequestStatus.PENDING_APPROVAL).count();
        long pendingExchanges = exchangeRequests.stream().filter(item -> item.getStatus() == ExchangeRequestStatus.PENDING_APPROVAL).count();
        long pendingStockOrders = stockOrders.stream().filter(item -> item.getStatus() == StockOrderStatus.PENDING_APPROVAL).count();
        long partiallyFilledOrders = stockOrders.stream().filter(item -> item.getStatus() == StockOrderStatus.PARTIALLY_FILLED).count();

        long staleFxPairs = latestFxRates.stream()
                .filter(item -> item.getEffectiveAt().isBefore(now.minus(MARKET_DATA_FRESHNESS_THRESHOLD)))
                .count();
        long staleStockQuotes = latestStockQuotes.stream()
                .filter(item -> item.getEffectiveAt().isBefore(now.minus(MARKET_DATA_FRESHNESS_THRESHOLD)))
                .count();

        BigDecimal pendingInstructionVolumeKrw = pendingInstructionVolumeKrw(fundingRequests, exchangeRequests, stockOrders, latestFxRates);

        return new AdminOverviewResponse(
                new AdminOverviewResponse.Metrics(
                        pendingApprovals,
                        overdueApprovals,
                        reviewRequiredCustomers,
                        lockedAccounts,
                        pendingFundingRequests,
                        pendingExchanges,
                        pendingStockOrders,
                        partiallyFilledOrders,
                        pendingInstructionVolumeKrw
                ),
                new AdminOverviewResponse.MarketStatus(
                        latestFxRates.stream().map(FxRate::getEffectiveAt).max(Instant::compareTo).orElse(null),
                        latestStockQuotes.stream().map(StockQuote::getEffectiveAt).max(Instant::compareTo).orElse(null),
                        latestFxRates.size() - staleFxPairs,
                        staleFxPairs,
                        latestStockQuotes.size() - staleStockQuotes,
                        staleStockQuotes
                ),
                buildAlerts(
                        pendingApprovals,
                        overdueApprovals,
                        reviewRequiredCustomers,
                        lockedAccounts,
                        pendingFundingRequests,
                        pendingExchanges,
                        pendingStockOrders,
                        partiallyFilledOrders,
                        staleFxPairs + staleStockQuotes,
                        pendingInstructionVolumeKrw
                )
        );
    }

    private BigDecimal pendingInstructionVolumeKrw(
            List<FundingRequest> fundingRequests,
            List<ExchangeRequest> exchangeRequests,
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

    private List<AdminOverviewResponse.Alert> buildAlerts(
            long pendingApprovals,
            long overdueApprovals,
            long reviewRequiredCustomers,
            long lockedAccounts,
            long pendingFundingRequests,
            long pendingExchanges,
            long pendingStockOrders,
            long partiallyFilledOrders,
            long staleMarketFeeds,
            BigDecimal pendingInstructionVolumeKrw
    ) {
        List<AdminOverviewResponse.Alert> alerts = new ArrayList<>();

        if (overdueApprovals > 0) {
            alerts.add(new AdminOverviewResponse.Alert(
                    "HIGH",
                    "승인 SLA 초과",
                    overdueApprovals + "건의 승인 요청이 2시간 이상 대기 중입니다.",
                    "/approvals"
            ));
        }
        if (reviewRequiredCustomers > 0) {
            alerts.add(new AdminOverviewResponse.Alert(
                    "HIGH",
                    "심사 필요 고객 누적",
                    reviewRequiredCustomers + "명의 고객이 추가 심사를 기다리고 있습니다.",
                    "/customers"
            ));
        }
        if (lockedAccounts > 0) {
            alerts.add(new AdminOverviewResponse.Alert(
                    "MEDIUM",
                    "잠금 계좌 모니터링 필요",
                    lockedAccounts + "개의 계좌가 LOCKED 상태입니다.",
                    "/accounts"
            ));
        }
        if (pendingFundingRequests > 0) {
            alerts.add(new AdminOverviewResponse.Alert(
                    "INFO",
                    "입출금 운영 큐 확인",
                    pendingFundingRequests + "건의 입출금 요청이 승인 대기 중입니다.",
                    "/funding-requests"
            ));
        }
        if (staleMarketFeeds > 0) {
            alerts.add(new AdminOverviewResponse.Alert(
                    "HIGH",
                    "시세 데이터 점검 필요",
                    staleMarketFeeds + "개의 FX/주식 시세가 기준 freshness를 초과했습니다.",
                    "/fx-rates"
            ));
        }
        if (partiallyFilledOrders > 0) {
            alerts.add(new AdminOverviewResponse.Alert(
                    "MEDIUM",
                    "부분 체결 주문 후속 처리 필요",
                    partiallyFilledOrders + "건의 주문이 PARTIALLY_FILLED 상태입니다.",
                    "/stock-orders"
            ));
        }
        if (pendingApprovals > 0 || pendingFundingRequests > 0 || pendingExchanges > 0 || pendingStockOrders > 0) {
            alerts.add(new AdminOverviewResponse.Alert(
                    "INFO",
                    "운영 큐 볼륨 확인",
                    "승인/입출금/환전/주식 주문 대기 물량은 총 " + pendingInstructionVolumeKrw.toPlainString() + " KRW 상당입니다.",
                    "/"
            ));
        }

        if (alerts.isEmpty()) {
            alerts.add(new AdminOverviewResponse.Alert(
                    "STABLE",
                    "운영 상태 안정",
                    "현재 주요 운영 큐와 데이터 freshness 기준은 안정 범위입니다.",
                    "/"
            ));
        }

        return alerts;
    }

    private List<FxRate> latestFxRates(List<FxRate> rates) {
        Map<String, FxRate> latestByPair = new LinkedHashMap<>();
        for (FxRate rate : rates) {
            latestByPair.putIfAbsent(rate.getBaseCurrency() + "/" + rate.getQuoteCurrency(), rate);
        }
        return List.copyOf(latestByPair.values());
    }

    private List<StockQuote> latestStockQuotes(List<StockQuote> quotes) {
        Map<String, StockQuote> latestBySymbol = new LinkedHashMap<>();
        for (StockQuote quote : quotes) {
            latestBySymbol.putIfAbsent(quote.getSymbol() + "/" + quote.getMarket(), quote);
        }
        return List.copyOf(latestBySymbol.values());
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
            if (fxRate.getBaseCurrency().equalsIgnoreCase("KRW") && fxRate.getQuoteCurrency().equalsIgnoreCase(currency) && fxRate.getRate().signum() > 0) {
                return amount.divide(fxRate.getRate(), 4, RoundingMode.HALF_UP);
            }
        }

        return amount.setScale(4, RoundingMode.HALF_UP);
    }
}
