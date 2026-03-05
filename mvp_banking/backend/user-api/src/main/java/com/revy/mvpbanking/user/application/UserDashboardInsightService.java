package com.revy.mvpbanking.user.application;

import com.revy.mvpbanking.account.domain.Account;
import com.revy.mvpbanking.account.domain.AccountRepository;
import com.revy.mvpbanking.account.domain.AccountStatus;
import com.revy.mvpbanking.audit.application.AuditLogService;
import com.revy.mvpbanking.audit.domain.AuditActionType;
import com.revy.mvpbanking.customer.domain.CustomerRepository;
import com.revy.mvpbanking.exchange.domain.ExchangeRequest;
import com.revy.mvpbanking.exchange.domain.ExchangeRequestRepository;
import com.revy.mvpbanking.exchange.domain.ExchangeRequestStatus;
import com.revy.mvpbanking.fx.domain.FxRate;
import com.revy.mvpbanking.fx.domain.FxRateRepository;
import com.revy.mvpbanking.funding.domain.FundingRequest;
import com.revy.mvpbanking.funding.domain.FundingRequestRepository;
import com.revy.mvpbanking.funding.domain.FundingRequestStatus;
import com.revy.mvpbanking.stock.application.StockPositionService;
import com.revy.mvpbanking.stock.application.ValuedStockPosition;
import com.revy.mvpbanking.stock.domain.StockOrder;
import com.revy.mvpbanking.stock.domain.StockOrderRepository;
import com.revy.mvpbanking.stock.domain.StockOrderStatus;
import com.revy.mvpbanking.user.presentation.UserDashboardInsightResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional(readOnly = true)
public class UserDashboardInsightService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final FundingRequestRepository fundingRequestRepository;
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final StockOrderRepository stockOrderRepository;
    private final StockPositionService stockPositionService;
    private final FxRateRepository fxRateRepository;
    private final AuditLogService auditLogService;

    public UserDashboardInsightService(
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            FundingRequestRepository fundingRequestRepository,
            ExchangeRequestRepository exchangeRequestRepository,
            StockOrderRepository stockOrderRepository,
            StockPositionService stockPositionService,
            FxRateRepository fxRateRepository,
            AuditLogService auditLogService
    ) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.fundingRequestRepository = fundingRequestRepository;
        this.exchangeRequestRepository = exchangeRequestRepository;
        this.stockOrderRepository = stockOrderRepository;
        this.stockPositionService = stockPositionService;
        this.fxRateRepository = fxRateRepository;
        this.auditLogService = auditLogService;
    }

    public UserDashboardInsightResponse getInsights(UUID endUserId) {
        UUID customerId = customerRepository.findByEndUserId(endUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"))
                .getId();

        auditLogService.logCurrentActor(AuditActionType.USER_DASHBOARD_VIEWED, "USER_DASHBOARD", customerId.toString(), "Viewed user dashboard insight");

        List<Account> accounts = accountRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        List<FundingRequest> fundingRequests = fundingRequestRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        List<ExchangeRequest> exchangeRequests = exchangeRequestRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        List<StockOrder> stockOrders = stockOrderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        List<ValuedStockPosition> positions = stockPositionService.getValuedPositionsByCustomerId(customerId);
        List<FxRate> latestFxRates = latestFxRates(fxRateRepository.findAllByOrderByEffectiveAtDesc());

        BigDecimal cashAssetsKrw = accounts.stream()
                .map(account -> convertToKrw(account.getBalance(), account.getCurrency(), latestFxRates))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);
        BigDecimal investmentAssetsKrw = positions.stream()
                .map(position -> convertToKrw(resolveMarketValue(position), position.currency(), latestFxRates))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);
        BigDecimal unrealizedProfitLossKrw = positions.stream()
                .map(position -> convertToKrw(nullSafe(position.unrealizedProfitLoss()), position.currency(), latestFxRates))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);
        BigDecimal realizedProfitLossKrw = positions.stream()
                .map(position -> convertToKrw(position.realizedProfitLoss(), position.currency(), latestFxRates))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);

        long activeAccountCount = accounts.stream().filter(account -> account.getStatus() == AccountStatus.ACTIVE).count();
        long pendingInstructionCount = fundingRequests.stream().filter(item -> item.getStatus() == FundingRequestStatus.PENDING_APPROVAL).count()
                + exchangeRequests.stream().filter(item -> item.getStatus() == ExchangeRequestStatus.PENDING_APPROVAL).count()
                + stockOrders.stream().filter(item -> item.getStatus() == StockOrderStatus.PENDING_APPROVAL).count();
        long partiallyFilledOrderCount = stockOrders.stream().filter(item -> item.getStatus() == StockOrderStatus.PARTIALLY_FILLED).count();

        return new UserDashboardInsightResponse(
                cashAssetsKrw.add(investmentAssetsKrw).setScale(4, RoundingMode.HALF_UP),
                cashAssetsKrw,
                investmentAssetsKrw,
                unrealizedProfitLossKrw,
                realizedProfitLossKrw,
                activeAccountCount,
                pendingInstructionCount,
                partiallyFilledOrderCount,
                buildAttentionItems(fundingRequests, exchangeRequests, stockOrders, positions, latestFxRates),
                buildCurrencyExposures(accounts, positions, latestFxRates),
                buildTopPositions(positions)
        );
    }

    private List<UserDashboardInsightResponse.AttentionItem> buildAttentionItems(
            List<FundingRequest> fundingRequests,
            List<ExchangeRequest> exchangeRequests,
            List<StockOrder> stockOrders,
            List<ValuedStockPosition> positions,
            List<FxRate> latestFxRates
    ) {
        List<UserDashboardInsightResponse.AttentionItem> items = new ArrayList<>();
        long pendingFundingCount = fundingRequests.stream().filter(item -> item.getStatus() == FundingRequestStatus.PENDING_APPROVAL).count();
        long pendingExchangeCount = exchangeRequests.stream().filter(item -> item.getStatus() == ExchangeRequestStatus.PENDING_APPROVAL).count();
        long pendingStockOrderCount = stockOrders.stream().filter(item -> item.getStatus() == StockOrderStatus.PENDING_APPROVAL).count();
        long partiallyFilledOrderCount = stockOrders.stream().filter(item -> item.getStatus() == StockOrderStatus.PARTIALLY_FILLED).count();

        if (pendingFundingCount > 0) {
            items.add(new UserDashboardInsightResponse.AttentionItem(
                    "INFO",
                    "입출금 승인 대기",
                    pendingFundingCount + "건의 입출금 요청이 운영 승인 대기 중입니다.",
                    "/funding-requests"
            ));
        }
        if (pendingExchangeCount > 0) {
            items.add(new UserDashboardInsightResponse.AttentionItem(
                    "INFO",
                    "환전 승인 대기",
                    pendingExchangeCount + "건의 환전 요청이 운영 승인 대기 중입니다.",
                    "/exchange-requests"
            ));
        }
        if (pendingStockOrderCount > 0) {
            items.add(new UserDashboardInsightResponse.AttentionItem(
                    "INFO",
                    "주식 주문 승인 대기",
                    pendingStockOrderCount + "건의 주식 주문이 접수 후 승인 대기 중입니다.",
                    "/stock-orders"
            ));
        }
        if (partiallyFilledOrderCount > 0) {
            items.add(new UserDashboardInsightResponse.AttentionItem(
                    "MEDIUM",
                    "부분 체결 주문 존재",
                    partiallyFilledOrderCount + "건의 주문이 부분 체결 상태입니다.",
                    "/stock-orders"
            ));
        }

        positions.stream()
                .filter(position -> position.unrealizedProfitLoss() != null && position.unrealizedProfitLoss().signum() < 0)
                .min((left, right) -> left.unrealizedProfitLoss().compareTo(right.unrealizedProfitLoss()))
                .ifPresent(position -> items.add(new UserDashboardInsightResponse.AttentionItem(
                        "WATCH",
                        "손실 모니터링 필요",
                        position.symbol() + " 평가손익이 "
                                + convertToKrw(position.unrealizedProfitLoss(), position.currency(), latestFxRates).toPlainString()
                                + " KRW 입니다.",
                        "/stock-positions"
                )));

        if (items.isEmpty()) {
            items.add(new UserDashboardInsightResponse.AttentionItem(
                    "STABLE",
                    "자산 상태 안정",
                    "현재 기준으로 대기 지시나 손실 경보 없이 안정적으로 유지되고 있습니다.",
                    "/"
            ));
        }

        return items;
    }

    private List<UserDashboardInsightResponse.CurrencyExposure> buildCurrencyExposures(
            List<Account> accounts,
            List<ValuedStockPosition> positions,
            List<FxRate> latestFxRates
    ) {
        Map<String, CurrencyBucket> buckets = new LinkedHashMap<>();

        for (Account account : accounts) {
            CurrencyBucket bucket = buckets.computeIfAbsent(account.getCurrency(), ignored -> new CurrencyBucket());
            bucket.cashBalance = bucket.cashBalance.add(account.getBalance());
        }
        for (ValuedStockPosition position : positions) {
            CurrencyBucket bucket = buckets.computeIfAbsent(position.currency(), ignored -> new CurrencyBucket());
            bucket.positionValue = bucket.positionValue.add(resolveMarketValue(position));
        }

        return buckets.entrySet().stream()
                .map(entry -> {
                    BigDecimal totalExposure = entry.getValue().cashBalance.add(entry.getValue().positionValue).setScale(4, RoundingMode.HALF_UP);
                    return new UserDashboardInsightResponse.CurrencyExposure(
                            entry.getKey(),
                            entry.getValue().cashBalance.setScale(4, RoundingMode.HALF_UP),
                            entry.getValue().positionValue.setScale(4, RoundingMode.HALF_UP),
                            totalExposure,
                            convertToKrw(totalExposure, entry.getKey(), latestFxRates)
                    );
                })
                .sorted((left, right) -> right.krwEquivalent().compareTo(left.krwEquivalent()))
                .toList();
    }

    private List<UserDashboardInsightResponse.TopPosition> buildTopPositions(List<ValuedStockPosition> positions) {
        return positions.stream()
                .sorted((left, right) -> resolveMarketValue(right).compareTo(resolveMarketValue(left)))
                .limit(3)
                .map(position -> new UserDashboardInsightResponse.TopPosition(
                        position.symbol(),
                        position.market(),
                        resolveMarketValue(position),
                        nullSafe(position.unrealizedProfitLoss()),
                        nullSafe(position.unrealizedProfitRate()),
                        position.currency()
                ))
                .toList();
    }

    private List<FxRate> latestFxRates(List<FxRate> rates) {
        Map<String, FxRate> latestByPair = new LinkedHashMap<>();
        for (FxRate rate : rates) {
            latestByPair.putIfAbsent(rate.getBaseCurrency() + "/" + rate.getQuoteCurrency(), rate);
        }
        return List.copyOf(latestByPair.values());
    }

    private BigDecimal resolveMarketValue(ValuedStockPosition position) {
        return position.marketValue() != null ? position.marketValue() : position.costBasis();
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

    private BigDecimal nullSafe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP) : value.setScale(4, RoundingMode.HALF_UP);
    }

    private static final class CurrencyBucket {
        private BigDecimal cashBalance = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        private BigDecimal positionValue = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
    }
}
