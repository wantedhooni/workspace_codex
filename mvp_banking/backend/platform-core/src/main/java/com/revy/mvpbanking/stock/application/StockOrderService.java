package com.revy.mvpbanking.stock.application;

import com.revy.mvpbanking.account.domain.Account;
import com.revy.mvpbanking.account.domain.AccountRepository;
import com.revy.mvpbanking.account.domain.AccountType;
import com.revy.mvpbanking.approval.domain.ApprovalRequest;
import com.revy.mvpbanking.approval.domain.ApprovalRequestRepository;
import com.revy.mvpbanking.approval.domain.ApprovalStatus;
import com.revy.mvpbanking.approval.domain.ApprovalTargetType;
import com.revy.mvpbanking.audit.application.AuditLogService;
import com.revy.mvpbanking.audit.domain.AuditActionType;
import com.revy.mvpbanking.auth.infrastructure.AuthUserPrincipal;
import com.revy.mvpbanking.customer.domain.CustomerRepository;
import com.revy.mvpbanking.notification.application.NotificationService;
import com.revy.mvpbanking.notification.domain.NotificationCategory;
import com.revy.mvpbanking.notification.domain.NotificationSeverity;
import com.revy.mvpbanking.stock.domain.StockOrder;
import com.revy.mvpbanking.stock.domain.StockOrderExecution;
import com.revy.mvpbanking.stock.domain.StockOrderExecutionRepository;
import com.revy.mvpbanking.stock.domain.StockOrderMarketSession;
import com.revy.mvpbanking.stock.domain.StockOrderRepository;
import com.revy.mvpbanking.stock.domain.StockOrderTimeInForce;
import com.revy.mvpbanking.stock.domain.StockPosition;
import com.revy.mvpbanking.stock.domain.StockPositionRepository;
import com.revy.mvpbanking.stock.domain.StockOrderSide;
import com.revy.mvpbanking.stock.domain.StockOrderStatus;
import com.revy.mvpbanking.stock.domain.StockQuote;
import com.revy.mvpbanking.stock.domain.StockQuoteRepository;
import com.revy.mvpbanking.transaction.domain.TransactionEntry;
import com.revy.mvpbanking.transaction.domain.TransactionEntryRepository;
import com.revy.mvpbanking.transaction.domain.TransactionStatus;
import com.revy.mvpbanking.transaction.domain.TransactionType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * 주식 주문의 생성, 취소, 승인/체결/정산을 담당하는 애플리케이션 서비스입니다.
 * <p>
 * 주문 시점 정책 스냅샷(TIF, 시장 세션, 예상 체결 시각, 수동 심사 사유)을 고정해
 * 운영 승인 과정과 사용자 화면에서 동일한 기준으로 주문 상태를 해석할 수 있게 합니다.
 */
@Service
public class StockOrderService {

    private static final BigDecimal TRADING_FEE_RATE = new BigDecimal("0.0015");
    private static final BigDecimal SELL_TAX_RATE = new BigDecimal("0.0023");
    private static final int MAX_ORDER_MEMO_LENGTH = 200;
    private static final int MAX_CANCEL_REASON_LENGTH = 255;
    private static final BigDecimal LARGE_NOTIONAL_REVIEW_THRESHOLD = new BigDecimal("50000.0000");
    private static final BigDecimal PRICE_DEVIATION_REVIEW_THRESHOLD = new BigDecimal("0.0300");
    private static final long QUOTE_STALENESS_REVIEW_MINUTES = 20;
    private static final ZoneId US_MARKET_ZONE = ZoneId.of("America/New_York");
    private static final LocalTime REGULAR_SESSION_OPEN = LocalTime.of(9, 30);
    private static final LocalTime REGULAR_SESSION_CLOSE = LocalTime.of(16, 0);

    private final StockOrderRepository stockOrderRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final StockQuoteRepository stockQuoteRepository;
    private final StockOrderExecutionRepository stockOrderExecutionRepository;
    private final StockPositionRepository stockPositionRepository;
    private final TransactionEntryRepository transactionEntryRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    public StockOrderService(
            StockOrderRepository stockOrderRepository,
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            ApprovalRequestRepository approvalRequestRepository,
            StockQuoteRepository stockQuoteRepository,
            StockOrderExecutionRepository stockOrderExecutionRepository,
            StockPositionRepository stockPositionRepository,
            TransactionEntryRepository transactionEntryRepository,
            AuditLogService auditLogService,
            NotificationService notificationService
    ) {
        this.stockOrderRepository = stockOrderRepository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.approvalRequestRepository = approvalRequestRepository;
        this.stockQuoteRepository = stockQuoteRepository;
        this.stockOrderExecutionRepository = stockOrderExecutionRepository;
        this.stockPositionRepository = stockPositionRepository;
        this.transactionEntryRepository = transactionEntryRepository;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
    }

    /**
     * 관리자 화면에서 사용할 주식 주문 목록(체결 이력 포함)을 최신순으로 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<StockOrderDetail> getAdminOrders() {
        auditLogService.logCurrentActor(AuditActionType.STOCK_ORDER_LIST_VIEWED, "STOCK_ORDER", "all", "Viewed stock orders");
        return attachExecutions(stockOrderRepository.findAllByOrderByCreatedAtDesc());
    }

    /**
     * 현재 사용자(고객)의 주식 주문 목록(체결 이력 포함)을 최신순으로 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<StockOrderDetail> getUserOrders(UUID endUserId) {
        UUID customerId = customerRepository.findByEndUserId(endUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"))
                .getId();
        return attachExecutions(stockOrderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId));
    }

    /**
     * 신규 주식 주문을 생성합니다.
     * <p>
     * 계좌/잔고/보유수량 검증, 정책 스냅샷 계산, 승인 큐 등록, 사용자/관리자 알림 발행을 함께 처리합니다.
     */
    @Transactional
    public StockOrder create(
            UUID endUserId,
            UUID accountId,
            String symbol,
            String market,
            StockOrderSide side,
            BigDecimal quantity,
            BigDecimal limitPrice,
            String currency,
            String orderMemo,
            StockOrderTimeInForce timeInForce
    ) {
        var customer = customerRepository.findByEndUserId(endUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"));
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Account not found"));

        if (!account.getCustomerId().equals(customer.getId())) {
            throw new ResponseStatusException(FORBIDDEN, "Account does not belong to current user");
        }
        if (account.getAccountType() != AccountType.SECURITIES) {
            throw new ResponseStatusException(BAD_REQUEST, "Stock trading requires securities account");
        }
        if (quantity == null || quantity.signum() <= 0 || limitPrice == null || limitPrice.signum() <= 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Quantity and price must be positive");
        }
        String normalizedSymbol = symbol.trim().toUpperCase();
        String normalizedMarket = market.trim().toUpperCase();
        String normalizedCurrency = currency.trim().toUpperCase();

        BigDecimal grossAmount = quantity.multiply(limitPrice).setScale(4, RoundingMode.HALF_UP);
        BigDecimal estimatedCashImpact = calculateSettlementAmounts(grossAmount, side).netSettlementAmount();
        if (side == StockOrderSide.BUY && account.getBalance().compareTo(estimatedCashImpact) < 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Insufficient cash balance for buy order");
        }
        if (side == StockOrderSide.SELL) {
            StockPosition position = stockPositionRepository.findByAccountIdAndSymbolIgnoreCase(accountId, normalizedSymbol)
                    .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "No stock position available for sell order"));
            if (position.getQuantity().compareTo(quantity) < 0) {
                throw new ResponseStatusException(BAD_REQUEST, "Insufficient stock position quantity");
            }
        }
        String sanitizedOrderMemo = sanitizeMemo(orderMemo, "Order memo");
        StockOrderTimeInForce normalizedTimeInForce = timeInForce == null
                ? StockOrderTimeInForce.DAY
                : timeInForce;
        Instant requestedAt = Instant.now();
        Optional<StockQuote> latestQuote = stockQuoteRepository
                .findTopBySymbolIgnoreCaseAndMarketIgnoreCaseOrderByEffectiveAtDesc(normalizedSymbol, normalizedMarket);
        StockOrderPolicyDecision policyDecision = evaluatePolicy(
                normalizedTimeInForce,
                normalizedMarket,
                grossAmount,
                limitPrice.setScale(4, RoundingMode.HALF_UP),
                requestedAt,
                latestQuote
        );
        if (policyDecision.timeInForce() == StockOrderTimeInForce.IOC
                && policyDecision.marketSession() != StockOrderMarketSession.REGULAR) {
            throw new ResponseStatusException(BAD_REQUEST, "IOC orders are only allowed during regular market session");
        }

        StockOrder order = stockOrderRepository.save(
                new StockOrder(
                        customer.getId(),
                        accountId,
                        "ORD-" + System.currentTimeMillis(),
                        normalizedSymbol,
                        normalizedMarket,
                        side,
                        quantity.setScale(4, RoundingMode.HALF_UP),
                        limitPrice.setScale(4, RoundingMode.HALF_UP),
                        grossAmount,
                        normalizedCurrency,
                        StockOrderStatus.PENDING_APPROVAL,
                        sanitizedOrderMemo,
                        policyDecision.timeInForce(),
                        policyDecision.expiresAt(),
                        policyDecision.marketSession(),
                        policyDecision.expectedExecutionAt(),
                        policyDecision.manualReviewRequired(),
                        policyDecision.manualReviewReason(),
                        policyDecision.referencePrice(),
                        policyDecision.priceDeviationRate(),
                        policyDecision.quoteEffectiveAt(),
                        policyDecision.quoteSource()
                )
        );

        approvalRequestRepository.save(
                new ApprovalRequest(
                        ApprovalTargetType.STOCK_ORDER,
                        order.getId(),
                        "Stock order " + order.getOrderNumber(),
                        buildApprovalDescription(order, policyDecision),
                        ApprovalStatus.PENDING,
                        customer.getEmail()
                )
        );

        auditLogService.logCurrentActor(
                AuditActionType.STOCK_ORDER_CREATED,
                "STOCK_ORDER",
                order.getId().toString(),
                "Created stock order " + order.getOrderNumber()
        );
        notificationService.notifyUser(
                "USER-STOCK-ORDER-CREATED:" + order.getId(),
                customer.getEndUserId(),
                NotificationCategory.STOCK_ORDER,
                NotificationSeverity.INFO,
                "주식 주문 접수",
                order.getOrderNumber() + " 주문이 접수되었고 운영 승인 대기 중입니다.",
                "/stock-orders",
                "STOCK_ORDER",
                order.getId()
        );
        notificationService.notifyActiveAdmins(
                "ADMIN-STOCK-ORDER-PENDING:" + order.getId(),
                NotificationCategory.STOCK_ORDER,
                NotificationSeverity.ACTION_REQUIRED,
                "신규 주식 주문",
                order.getOrderNumber() + " 주문이 승인 대기열에 추가되었습니다.",
                "/approvals",
                "STOCK_ORDER",
                order.getId()
        );
        return order;
    }

    /**
     * 사용자가 대기중 또는 부분체결 주문을 취소합니다.
     */
    @Transactional
    public StockOrder cancelByUser(UUID endUserId, UUID orderId, String cancelReason) {
        var customer = customerRepository.findByEndUserId(endUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"));
        StockOrder stockOrder = stockOrderRepository.findByIdAndCustomerId(orderId, customer.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Stock order not found"));

        String normalizedReason = sanitizeText(cancelReason, "Cancel reason", MAX_CANCEL_REASON_LENGTH);
        if (normalizedReason == null) {
            normalizedReason = "사용자 요청 취소";
        }
        try {
            stockOrder.cancel(normalizedReason);
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(CONFLICT, exception.getMessage());
        }

        final String decisionReason = normalizedReason;
        approvalRequestRepository.findByTargetTypeAndTargetIdAndStatus(
                        ApprovalTargetType.STOCK_ORDER,
                        stockOrder.getId(),
                        ApprovalStatus.PENDING
                )
                .ifPresent(approvalRequest -> approvalRequest.cancel(
                        customer.getEmail(),
                        "사용자 취소: " + decisionReason
                ));

        auditLogService.logCurrentActor(
                AuditActionType.STOCK_ORDER_CANCELED,
                "STOCK_ORDER",
                stockOrder.getId().toString(),
                "Canceled stock order " + stockOrder.getOrderNumber()
        );
        notificationService.notifyActiveAdmins(
                "ADMIN-STOCK-ORDER-CANCELED:" + stockOrder.getId(),
                NotificationCategory.STOCK_ORDER,
                NotificationSeverity.INFO,
                "사용자 주식 주문 취소",
                stockOrder.getOrderNumber() + " 주문이 사용자 요청으로 취소되었습니다.",
                "/stock-orders",
                "STOCK_ORDER",
                stockOrder.getId()
        );
        return stockOrder;
    }

    /**
     * 주문 승인 시 초기 체결 수량을 계산해 체결/정산을 수행합니다.
     */
    @Transactional
    public void markApproved(UUID orderId) {
        StockOrder order = stockOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Stock order not found"));
        if (order.getStatus() == StockOrderStatus.APPROVED) {
            return;
        }
        if (order.getStatus() == StockOrderStatus.PARTIALLY_FILLED) {
            throw new ResponseStatusException(CONFLICT, "Order already approved and waiting for remaining fill");
        }
        if (order.getStatus() == StockOrderStatus.CANCELED) {
            throw new ResponseStatusException(CONFLICT, "Canceled order cannot be approved");
        }
        executeOrder(order, determineInitialExecutionQuantity(order), Instant.now());
    }

    /**
     * 부분체결 상태 주문의 잔여 수량을 운영자가 강제로 체결 완료합니다.
     */
    @Transactional
    public void completeRemainingFill(UUID orderId) {
        StockOrder order = stockOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Stock order not found"));
        if (order.getStatus() != StockOrderStatus.PARTIALLY_FILLED) {
            throw new ResponseStatusException(CONFLICT, "Only partially filled orders can be completed");
        }
        executeOrder(order, order.getRemainingQuantity(), Instant.now());
    }

    /**
     * 주문을 반려 상태로 전환하고 사용자 알림을 전송합니다.
     */
    @Transactional
    public void markRejected(UUID orderId) {
        StockOrder stockOrder = stockOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Stock order not found"));
        if (stockOrder.getStatus() == StockOrderStatus.CANCELED) {
            throw new ResponseStatusException(CONFLICT, "Canceled order cannot be rejected");
        }
        stockOrder.reject();
        customerRepository.findById(stockOrder.getCustomerId())
                .map(customer -> customer.getEndUserId())
                .ifPresent(endUserId -> notificationService.notifyUser(
                        "USER-STOCK-ORDER-REJECTED:" + stockOrder.getId(),
                        endUserId,
                        NotificationCategory.STOCK_ORDER,
                        NotificationSeverity.WARNING,
                        "주식 주문 반려",
                        stockOrder.getOrderNumber() + " 주문이 반려되었습니다.",
                        "/stock-orders",
                        "STOCK_ORDER",
                        stockOrder.getId()
                ));
    }

    private List<StockOrderDetail> attachExecutions(List<StockOrder> orders) {
        if (orders.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<StockOrderExecution>> executionsByOrderId = stockOrderExecutionRepository
                .findByOrderIdInOrderByExecutedAtDesc(orders.stream().map(StockOrder::getId).toList())
                .stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        StockOrderExecution::getOrderId,
                        java.util.LinkedHashMap::new,
                        java.util.stream.Collectors.toList()
                ));

        return orders.stream()
                .map(order -> new StockOrderDetail(order, executionsByOrderId.getOrDefault(order.getId(), List.of())))
                .toList();
    }

    /**
     * 지정 수량만큼 주문을 체결하고 포지션/계좌/거래 원장/알림을 갱신합니다.
     */
    private void executeOrder(StockOrder order, BigDecimal quantityToExecute, Instant settledAt) {
        if (order.getStatus() == StockOrderStatus.REJECTED) {
            throw new ResponseStatusException(CONFLICT, "Rejected order cannot be executed");
        }
        if (order.getStatus() == StockOrderStatus.CANCELED) {
            throw new ResponseStatusException(CONFLICT, "Canceled order cannot be executed");
        }
        if (quantityToExecute == null || quantityToExecute.signum() <= 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Execution quantity must be positive");
        }
        if (order.getRemainingQuantity() != null && quantityToExecute.compareTo(order.getRemainingQuantity()) > 0) {
            throw new ResponseStatusException(CONFLICT, "Execution quantity exceeds remaining quantity");
        }

        Account account = accountRepository.findById(order.getAccountId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Account not found"));
        List<StockOrderExecution> existingExecutions = stockOrderExecutionRepository.findByOrderIdOrderByExecutedAtDesc(order.getId());
        List<ExecutionSlice> executionSlices = buildExecutionSlices(order, quantityToExecute, settledAt);

        StockPosition position = stockPositionRepository.findByAccountIdAndSymbolIgnoreCase(order.getAccountId(), order.getSymbol())
                .orElseGet(() -> new StockPosition(
                        order.getCustomerId(),
                        order.getAccountId(),
                        order.getSymbol(),
                        order.getMarket(),
                        BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP),
                        BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP),
                        order.getCurrency()
                ));

        try {
            for (ExecutionSlice executionSlice : executionSlices) {
                SettlementAmounts settlementAmounts = calculateSettlementAmounts(executionSlice.executedAmount(), order.getSide());
                if (order.getSide() == StockOrderSide.BUY) {
                    account.debit(settlementAmounts.netSettlementAmount());
                    position.buy(executionSlice.executedQuantity(), executionSlice.executedPrice());
                } else {
                    position.sell(executionSlice.executedQuantity(), executionSlice.executedPrice());
                    account.credit(settlementAmounts.netSettlementAmount());
                }
            }
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(CONFLICT, exception.getMessage());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(BAD_REQUEST, exception.getMessage());
        }

        stockPositionRepository.save(position);
        persistExecutions(order, executionSlices, existingExecutions.size());

        List<ExistingExecutionView> existingExecutionViews = ExistingExecutionView.from(existingExecutions);

        BigDecimal cumulativeExecutedAmount = totalExecutedAmount(existingExecutionViews)
                .add(totalExecutedAmount(executionSlices))
                .setScale(4, RoundingMode.HALF_UP);
        BigDecimal cumulativeExecutedQuantity = totalExecutedQuantity(existingExecutionViews)
                .add(totalExecutedQuantity(executionSlices))
                .setScale(4, RoundingMode.HALF_UP);
        BigDecimal cumulativeFeeAmount = order.getFeeAmount()
                .add(totalFeeAmount(executionSlices, order.getSide()))
                .setScale(4, RoundingMode.HALF_UP);
        BigDecimal cumulativeTaxAmount = order.getTaxAmount()
                .add(totalTaxAmount(executionSlices, order.getSide()))
                .setScale(4, RoundingMode.HALF_UP);
        BigDecimal cumulativeNetSettlementAmount = order.getNetSettlementAmount()
                .add(totalNetSettlementAmount(executionSlices, order.getSide()))
                .setScale(4, RoundingMode.HALF_UP);
        String transactionNumber = "TXN-ORD-" + order.getOrderNumber();

        upsertSettlementTransaction(
                account,
                order,
                transactionNumber,
                cumulativeNetSettlementAmount,
                cumulativeExecutedQuantity,
                cumulativeFeeAmount,
                cumulativeTaxAmount,
                existingExecutions.size() + executionSlices.size(),
                settledAt
        );

        order.applyExecution(
                cumulativeExecutedQuantity,
                cumulativeExecutedAmount.divide(cumulativeExecutedQuantity, 4, RoundingMode.HALF_UP),
                cumulativeFeeAmount,
                cumulativeTaxAmount,
                cumulativeNetSettlementAmount,
                transactionNumber,
                settledAt
        );
        logExecutionStatus(order);
    }

    private void persistExecutions(StockOrder order, List<ExecutionSlice> executionSlices, int existingExecutionCount) {
        for (int index = 0; index < executionSlices.size(); index++) {
            ExecutionSlice executionSlice = executionSlices.get(index);
            int sequence = existingExecutionCount + index + 1;
            String executionNumber = "EXE-" + order.getOrderNumber() + "-" + String.format("%02d", sequence);
            stockOrderExecutionRepository.findByExecutionNumber(executionNumber)
                    .orElseGet(() -> stockOrderExecutionRepository.save(
                            new StockOrderExecution(
                                    order.getId(),
                                    executionNumber,
                                    sequence,
                                    executionSlice.executedQuantity(),
                                    executionSlice.executedPrice(),
                                    executionSlice.executedAmount(),
                                    executionSlice.executedAt()
                            )
                    ));
        }
    }

    private void upsertSettlementTransaction(
            Account account,
            StockOrder order,
            String transactionNumber,
            BigDecimal cumulativeNetSettlementAmount,
            BigDecimal cumulativeExecutedQuantity,
            BigDecimal cumulativeFeeAmount,
            BigDecimal cumulativeTaxAmount,
            int executionCount,
            Instant settledAt
    ) {
        String description = order.getSide().name()
                + " "
                + order.getSymbol()
                + " "
                + cumulativeExecutedQuantity.toPlainString()
                + "/"
                + order.getQuantity().toPlainString()
                + " with "
                + executionCount
                + " fills"
                + " / fee "
                + cumulativeFeeAmount.toPlainString()
                + (cumulativeTaxAmount.signum() > 0 ? " / tax " + cumulativeTaxAmount.toPlainString() : "");

        transactionEntryRepository.findByTransactionNumber(transactionNumber)
                .ifPresentOrElse(
                        transactionEntry -> transactionEntry.updateSettlement(cumulativeNetSettlementAmount, description, settledAt),
                        () -> transactionEntryRepository.save(
                                new TransactionEntry(
                                        account.getId(),
                                        transactionNumber,
                                        order.getSide() == StockOrderSide.BUY ? TransactionType.BUY : TransactionType.SELL,
                                        TransactionStatus.COMPLETED,
                                        cumulativeNetSettlementAmount,
                                        order.getCurrency(),
                                        description,
                                        settledAt
                                )
                        )
                );
    }

    private void logExecutionStatus(StockOrder order) {
        AuditActionType actionType = order.getStatus() == StockOrderStatus.APPROVED
                ? AuditActionType.STOCK_ORDER_FILLED
                : AuditActionType.STOCK_ORDER_PARTIALLY_FILLED;
        String description = order.getStatus() == StockOrderStatus.APPROVED
                ? "Filled stock order " + order.getOrderNumber()
                : "Partially filled stock order " + order.getOrderNumber();

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthUserPrincipal) {
            auditLogService.logCurrentActor(actionType, "STOCK_ORDER", order.getId().toString(), description);
        } else {
            auditLogService.logSystem(actionType, "STOCK_ORDER", order.getId().toString(), description);
        }

        customerRepository.findById(order.getCustomerId())
                .map(customer -> customer.getEndUserId())
                .ifPresent(endUserId -> notificationService.notifyUser(
                        order.getStatus() == StockOrderStatus.APPROVED
                                ? "USER-STOCK-ORDER-FILLED:" + order.getId()
                                : "USER-STOCK-ORDER-PARTIAL:" + order.getId() + ":" + (order.getExecutedQuantity() == null ? "0" : order.getExecutedQuantity().toPlainString()),
                        endUserId,
                        NotificationCategory.STOCK_ORDER,
                        order.getStatus() == StockOrderStatus.APPROVED ? NotificationSeverity.SUCCESS : NotificationSeverity.ACTION_REQUIRED,
                        order.getStatus() == StockOrderStatus.APPROVED ? "주식 주문 체결 완료" : "주식 주문 부분 체결",
                        order.getStatus() == StockOrderStatus.APPROVED
                                ? order.getOrderNumber() + " 주문이 모두 체결되었습니다."
                                : order.getOrderNumber() + " 주문이 일부 체결되었으며 잔여 수량이 남아 있습니다.",
                        "/stock-orders",
                        "STOCK_ORDER",
                        order.getId()
                ));
    }

    private static BigDecimal determineInitialExecutionQuantity(StockOrder order) {
        if (order.getSide() == StockOrderSide.SELL || order.getQuantity().compareTo(new BigDecimal("8.0000")) < 0) {
            return order.getQuantity();
        }

        BigDecimal initialQuantity = order.getQuantity()
                .multiply(new BigDecimal("0.6500"))
                .setScale(4, RoundingMode.HALF_UP);
        BigDecimal remainingQuantity = order.getQuantity().subtract(initialQuantity).setScale(4, RoundingMode.HALF_UP);
        if (initialQuantity.signum() == 0 || remainingQuantity.compareTo(new BigDecimal("1.0000")) < 0) {
            return order.getQuantity();
        }
        return initialQuantity;
    }

    private static List<ExecutionSlice> buildExecutionSlices(StockOrder order, BigDecimal quantityToExecute, Instant settledAt) {
        if (quantityToExecute.compareTo(new BigDecimal("5.0000")) < 0) {
            return List.of(new ExecutionSlice(
                    quantityToExecute,
                    order.getLimitPrice(),
                    quantityToExecute.multiply(order.getLimitPrice()).setScale(4, RoundingMode.HALF_UP),
                    settledAt
            ));
        }

        BigDecimal firstQuantity = quantityToExecute
                .multiply(new BigDecimal("0.6000"))
                .setScale(4, RoundingMode.HALF_UP);
        BigDecimal secondQuantity = quantityToExecute.subtract(firstQuantity).setScale(4, RoundingMode.HALF_UP);
        if (firstQuantity.signum() == 0 || secondQuantity.signum() == 0) {
            return List.of(new ExecutionSlice(
                    quantityToExecute,
                    order.getLimitPrice(),
                    quantityToExecute.multiply(order.getLimitPrice()).setScale(4, RoundingMode.HALF_UP),
                    settledAt
            ));
        }

        BigDecimal firstPrice = adjustExecutionPrice(order.getLimitPrice(), order.getSide(), true);
        BigDecimal secondPrice = adjustExecutionPrice(order.getLimitPrice(), order.getSide(), false);
        return List.of(
                new ExecutionSlice(
                        firstQuantity,
                        firstPrice,
                        firstQuantity.multiply(firstPrice).setScale(4, RoundingMode.HALF_UP),
                        settledAt.minusSeconds(20)
                ),
                new ExecutionSlice(
                        secondQuantity,
                        secondPrice,
                        secondQuantity.multiply(secondPrice).setScale(4, RoundingMode.HALF_UP),
                        settledAt.minusSeconds(5)
                )
        ).stream()
                .sorted(Comparator.comparing(ExecutionSlice::executedAt))
                .toList();
    }

    private static BigDecimal adjustExecutionPrice(BigDecimal limitPrice, StockOrderSide side, boolean firstFill) {
        BigDecimal factor;
        if (side == StockOrderSide.BUY) {
            factor = new BigDecimal(firstFill ? "0.9985" : "0.9995");
        } else {
            factor = new BigDecimal(firstFill ? "1.0010" : "1.0025");
        }
        return limitPrice.multiply(factor).setScale(4, RoundingMode.HALF_UP);
    }

    private static BigDecimal totalExecutedAmount(List<? extends ExecutionQuantityAmount> executions) {
        return executions.stream()
                .map(ExecutionQuantityAmount::executedAmount)
                .reduce(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP), BigDecimal::add);
    }

    private static BigDecimal totalExecutedQuantity(List<? extends ExecutionQuantityAmount> executions) {
        return executions.stream()
                .map(ExecutionQuantityAmount::executedQuantity)
                .reduce(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP), BigDecimal::add);
    }

    private static BigDecimal totalFeeAmount(List<ExecutionSlice> executionSlices, StockOrderSide side) {
        return executionSlices.stream()
                .map(executionSlice -> calculateSettlementAmounts(executionSlice.executedAmount(), side).feeAmount())
                .reduce(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP), BigDecimal::add);
    }

    private static BigDecimal totalTaxAmount(List<ExecutionSlice> executionSlices, StockOrderSide side) {
        return executionSlices.stream()
                .map(executionSlice -> calculateSettlementAmounts(executionSlice.executedAmount(), side).taxAmount())
                .reduce(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP), BigDecimal::add);
    }

    private static BigDecimal totalNetSettlementAmount(List<ExecutionSlice> executionSlices, StockOrderSide side) {
        return executionSlices.stream()
                .map(executionSlice -> calculateSettlementAmounts(executionSlice.executedAmount(), side).netSettlementAmount())
                .reduce(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP), BigDecimal::add);
    }

    private static String sanitizeMemo(String memo, String label) {
        return sanitizeText(memo, label, MAX_ORDER_MEMO_LENGTH);
    }

    private static String sanitizeText(String input, String label, int maxLength) {
        if (input == null) {
            return null;
        }
        String normalized = input.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > maxLength) {
            throw new ResponseStatusException(BAD_REQUEST, label + " must be " + maxLength + " characters or less");
        }
        return normalized;
    }

    /**
     * 주문 시점 기준 정책 스냅샷(TIF, 세션, 만료, 예상 체결, 심사 사유)을 계산합니다.
     */
    private static StockOrderPolicyDecision evaluatePolicy(
            StockOrderTimeInForce timeInForce,
            String market,
            BigDecimal grossAmount,
            BigDecimal limitPrice,
            Instant requestedAt,
            Optional<StockQuote> latestQuote
    ) {
        StockOrderMarketSession marketSession = resolveMarketSession(market, requestedAt);
        Instant expectedExecutionAt = resolveExpectedExecutionAt(marketSession, market, requestedAt);
        Instant expiresAt = resolveExpiresAt(timeInForce, marketSession, market, requestedAt);
        List<String> reviewReasons = new ArrayList<>();

        if (grossAmount.compareTo(LARGE_NOTIONAL_REVIEW_THRESHOLD) >= 0) {
            reviewReasons.add("고액 주문 심사");
        }
        if (marketSession != StockOrderMarketSession.REGULAR) {
            reviewReasons.add("장외 시간 주문");
        }

        BigDecimal referencePrice = null;
        BigDecimal priceDeviationRate = null;
        Instant quoteEffectiveAt = null;
        String quoteSource = null;

        if (latestQuote.isPresent()) {
            StockQuote quote = latestQuote.get();
            referencePrice = quote.getPrice();
            quoteEffectiveAt = quote.getEffectiveAt();
            quoteSource = quote.getSource();
            priceDeviationRate = calculatePriceDeviationRate(limitPrice, referencePrice);
            if (priceDeviationRate != null && priceDeviationRate.compareTo(PRICE_DEVIATION_REVIEW_THRESHOLD) >= 0) {
                reviewReasons.add("시세 대비 지정가 괴리");
            }
            long quoteAgeMinutes = Math.max(0, ChronoUnit.MINUTES.between(quote.getEffectiveAt(), requestedAt));
            if (quoteAgeMinutes >= QUOTE_STALENESS_REVIEW_MINUTES) {
                reviewReasons.add("시세 지연 확인");
            }
        } else {
            reviewReasons.add("실시간 시세 미확인");
        }

        boolean manualReviewRequired = !reviewReasons.isEmpty();
        String manualReviewReason = manualReviewRequired ? String.join(" / ", reviewReasons) : null;
        return new StockOrderPolicyDecision(
                timeInForce,
                expiresAt,
                marketSession,
                expectedExecutionAt,
                manualReviewRequired,
                manualReviewReason,
                referencePrice,
                priceDeviationRate,
                quoteEffectiveAt,
                quoteSource
        );
    }

    private static StockOrderMarketSession resolveMarketSession(String market, Instant requestedAt) {
        ZoneId zoneId = resolveMarketZone(market);
        if (zoneId == null) {
            return StockOrderMarketSession.REGULAR;
        }
        ZonedDateTime marketNow = requestedAt.atZone(zoneId);
        if (!isBusinessDay(marketNow.toLocalDate())) {
            return StockOrderMarketSession.CLOSED;
        }
        LocalTime current = marketNow.toLocalTime();
        if (current.isBefore(REGULAR_SESSION_OPEN)) {
            return StockOrderMarketSession.PRE_MARKET;
        }
        if (current.isBefore(REGULAR_SESSION_CLOSE)) {
            return StockOrderMarketSession.REGULAR;
        }
        return StockOrderMarketSession.AFTER_HOURS;
    }

    private static Instant resolveExpectedExecutionAt(StockOrderMarketSession marketSession, String market, Instant requestedAt) {
        ZoneId zoneId = resolveMarketZone(market);
        if (zoneId == null || marketSession == StockOrderMarketSession.REGULAR) {
            return requestedAt.plusSeconds(30);
        }

        ZonedDateTime marketNow = requestedAt.atZone(zoneId);
        if (marketSession == StockOrderMarketSession.PRE_MARKET) {
            return marketNow.toLocalDate().atTime(REGULAR_SESSION_OPEN).atZone(zoneId).toInstant();
        }
        LocalDate nextOpenDate = nextBusinessDay(marketNow.toLocalDate());
        return nextOpenDate.atTime(REGULAR_SESSION_OPEN).atZone(zoneId).toInstant();
    }

    private static ZoneId resolveMarketZone(String market) {
        if ("NASDAQ".equalsIgnoreCase(market) || "NYSE".equalsIgnoreCase(market)) {
            return US_MARKET_ZONE;
        }
        return null;
    }

    private static LocalDate nextBusinessDay(LocalDate baseDate) {
        LocalDate next = baseDate.plusDays(1);
        while (!isBusinessDay(next)) {
            next = next.plusDays(1);
        }
        return next;
    }

    private static boolean isBusinessDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    private static BigDecimal calculatePriceDeviationRate(BigDecimal limitPrice, BigDecimal referencePrice) {
        if (referencePrice == null || referencePrice.signum() == 0) {
            return null;
        }
        return limitPrice.subtract(referencePrice).abs().divide(referencePrice, 6, RoundingMode.HALF_UP);
    }

    private static String buildApprovalDescription(StockOrder order, StockOrderPolicyDecision policyDecision) {
        StringBuilder description = new StringBuilder()
                .append(order.getSide().name())
                .append(" ")
                .append(order.getSymbol())
                .append(" on ")
                .append(order.getMarket());
        description.append(" / tif: ").append(policyDecision.timeInForce().name());
        description.append(" / expires: ").append(policyDecision.expiresAt());
        description.append(" / session: ").append(policyDecision.marketSession().name());
        if (policyDecision.manualReviewRequired()) {
            description.append(" / review: ").append(policyDecision.manualReviewReason());
        }
        return description.toString();
    }

    private static SettlementAmounts calculateSettlementAmounts(BigDecimal executedGrossAmount, StockOrderSide side) {
        BigDecimal feeAmount = executedGrossAmount.multiply(TRADING_FEE_RATE).setScale(4, RoundingMode.HALF_UP);
        BigDecimal taxAmount = side == StockOrderSide.SELL
                ? executedGrossAmount.multiply(SELL_TAX_RATE).setScale(4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        BigDecimal netSettlementAmount = side == StockOrderSide.BUY
                ? executedGrossAmount.add(feeAmount).setScale(4, RoundingMode.HALF_UP)
                : executedGrossAmount.subtract(feeAmount).subtract(taxAmount).setScale(4, RoundingMode.HALF_UP);
        return new SettlementAmounts(feeAmount, taxAmount, netSettlementAmount);
    }

    private sealed interface ExecutionQuantityAmount permits ExecutionSlice, ExistingExecutionView {
        BigDecimal executedQuantity();
        BigDecimal executedAmount();
    }

    private record ExistingExecutionView(
            BigDecimal executedQuantity,
            BigDecimal executedAmount
    ) implements ExecutionQuantityAmount {
        static List<ExistingExecutionView> from(List<StockOrderExecution> executions) {
            List<ExistingExecutionView> items = new ArrayList<>(executions.size());
            for (StockOrderExecution execution : executions) {
                items.add(new ExistingExecutionView(execution.getExecutedQuantity(), execution.getExecutedAmount()));
            }
            return items;
        }
    }

    private record ExecutionSlice(
            BigDecimal executedQuantity,
            BigDecimal executedPrice,
            BigDecimal executedAmount,
            Instant executedAt
    ) implements ExecutionQuantityAmount {
    }

    private record SettlementAmounts(
            BigDecimal feeAmount,
            BigDecimal taxAmount,
            BigDecimal netSettlementAmount
    ) {
    }

    private record StockOrderPolicyDecision(
            StockOrderTimeInForce timeInForce,
            Instant expiresAt,
            StockOrderMarketSession marketSession,
            Instant expectedExecutionAt,
            boolean manualReviewRequired,
            String manualReviewReason,
            BigDecimal referencePrice,
            BigDecimal priceDeviationRate,
            Instant quoteEffectiveAt,
            String quoteSource
    ) {
    }

    private static Instant resolveExpiresAt(
            StockOrderTimeInForce timeInForce,
            StockOrderMarketSession marketSession,
            String market,
            Instant requestedAt
    ) {
        if (timeInForce == StockOrderTimeInForce.IOC) {
            return requestedAt.plus(2, ChronoUnit.MINUTES);
        }
        if (timeInForce == StockOrderTimeInForce.GTC) {
            return requestedAt.plus(30, ChronoUnit.DAYS);
        }

        ZoneId zoneId = resolveMarketZone(market);
        if (zoneId == null) {
            return requestedAt.plus(1, ChronoUnit.DAYS);
        }

        ZonedDateTime marketNow = requestedAt.atZone(zoneId);
        LocalDate expiryDate = marketNow.toLocalDate();
        if (!isBusinessDay(expiryDate)
                || marketSession == StockOrderMarketSession.AFTER_HOURS
                || marketSession == StockOrderMarketSession.CLOSED) {
            expiryDate = nextBusinessDay(expiryDate);
        }
        return expiryDate.atTime(REGULAR_SESSION_CLOSE).atZone(zoneId).toInstant();
    }
}
