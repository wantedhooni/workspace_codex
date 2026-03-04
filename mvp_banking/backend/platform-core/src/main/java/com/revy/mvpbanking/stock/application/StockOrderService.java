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
import com.revy.mvpbanking.stock.domain.StockOrderRepository;
import com.revy.mvpbanking.stock.domain.StockPosition;
import com.revy.mvpbanking.stock.domain.StockPositionRepository;
import com.revy.mvpbanking.stock.domain.StockOrderSide;
import com.revy.mvpbanking.stock.domain.StockOrderStatus;
import com.revy.mvpbanking.transaction.domain.TransactionEntry;
import com.revy.mvpbanking.transaction.domain.TransactionEntryRepository;
import com.revy.mvpbanking.transaction.domain.TransactionStatus;
import com.revy.mvpbanking.transaction.domain.TransactionType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class StockOrderService {

    private static final BigDecimal TRADING_FEE_RATE = new BigDecimal("0.0015");
    private static final BigDecimal SELL_TAX_RATE = new BigDecimal("0.0023");

    private final StockOrderRepository stockOrderRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
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
        this.stockOrderExecutionRepository = stockOrderExecutionRepository;
        this.stockPositionRepository = stockPositionRepository;
        this.transactionEntryRepository = transactionEntryRepository;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<StockOrderDetail> getAdminOrders() {
        auditLogService.logCurrentActor(AuditActionType.STOCK_ORDER_LIST_VIEWED, "STOCK_ORDER", "all", "Viewed stock orders");
        return attachExecutions(stockOrderRepository.findAllByOrderByCreatedAtDesc());
    }

    @Transactional(readOnly = true)
    public List<StockOrderDetail> getUserOrders(UUID endUserId) {
        UUID customerId = customerRepository.findByEndUserId(endUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"))
                .getId();
        return attachExecutions(stockOrderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId));
    }

    @Transactional
    public StockOrder create(
            UUID endUserId,
            UUID accountId,
            String symbol,
            String market,
            StockOrderSide side,
            BigDecimal quantity,
            BigDecimal limitPrice,
            String currency
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

        BigDecimal grossAmount = quantity.multiply(limitPrice).setScale(4, RoundingMode.HALF_UP);
        BigDecimal estimatedCashImpact = calculateSettlementAmounts(grossAmount, side).netSettlementAmount();
        if (side == StockOrderSide.BUY && account.getBalance().compareTo(estimatedCashImpact) < 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Insufficient cash balance for buy order");
        }
        if (side == StockOrderSide.SELL) {
            StockPosition position = stockPositionRepository.findByAccountIdAndSymbolIgnoreCase(accountId, symbol)
                    .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "No stock position available for sell order"));
            if (position.getQuantity().compareTo(quantity) < 0) {
                throw new ResponseStatusException(BAD_REQUEST, "Insufficient stock position quantity");
            }
        }

        StockOrder order = stockOrderRepository.save(
                new StockOrder(
                        customer.getId(),
                        accountId,
                        "ORD-" + System.currentTimeMillis(),
                        symbol.toUpperCase(),
                        market.toUpperCase(),
                        side,
                        quantity.setScale(4, RoundingMode.HALF_UP),
                        limitPrice.setScale(4, RoundingMode.HALF_UP),
                        grossAmount,
                        currency.toUpperCase(),
                        StockOrderStatus.PENDING_APPROVAL
                )
        );

        approvalRequestRepository.save(
                new ApprovalRequest(
                        ApprovalTargetType.STOCK_ORDER,
                        order.getId(),
                        "Stock order " + order.getOrderNumber(),
                        order.getSide().name() + " " + order.getSymbol() + " on " + order.getMarket(),
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
        executeOrder(order, determineInitialExecutionQuantity(order), Instant.now());
    }

    @Transactional
    public void completeRemainingFill(UUID orderId) {
        StockOrder order = stockOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Stock order not found"));
        if (order.getStatus() != StockOrderStatus.PARTIALLY_FILLED) {
            throw new ResponseStatusException(CONFLICT, "Only partially filled orders can be completed");
        }
        executeOrder(order, order.getRemainingQuantity(), Instant.now());
    }

    @Transactional
    public void markRejected(UUID orderId) {
        StockOrder stockOrder = stockOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Stock order not found"));
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

    private void executeOrder(StockOrder order, BigDecimal quantityToExecute, Instant settledAt) {
        if (order.getStatus() == StockOrderStatus.REJECTED) {
            throw new ResponseStatusException(CONFLICT, "Rejected order cannot be executed");
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
}
