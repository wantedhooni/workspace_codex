package com.quant.mvp.pipeline.service;

import com.quant.mvp.pipeline.domain.Order;
import com.quant.mvp.pipeline.domain.OrderAuditLog;
import com.quant.mvp.pipeline.domain.OrderSide;
import com.quant.mvp.pipeline.domain.OrderStatus;
import com.quant.mvp.pipeline.domain.OrderType;
import com.quant.mvp.pipeline.domain.PortfolioSummary;
import com.quant.mvp.pipeline.domain.Position;
import com.quant.mvp.pipeline.domain.RiskLimit;
import com.quant.mvp.pipeline.domain.TimeInForce;
import com.quant.mvp.pipeline.domain.Trade;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class OrderTradePositionPipelineService {

    private static final Pattern US_SYMBOL_PATTERN = Pattern.compile("^[A-Z][A-Z0-9.-]{0,9}$");
    private static final BigDecimal MAX_ORDER_QUANTITY = new BigDecimal("1000000");
    private static final BigDecimal MAX_TRADE_PRICE = new BigDecimal("1000000");
    private static final BigDecimal DEFAULT_PRICE = new BigDecimal("100");
    private static final BigDecimal BPS_DENOMINATOR = new BigDecimal("10000");
    private static final BigDecimal TURNOVER_WARN_PCT = new BigDecimal("80");
    private static final BigDecimal TURNOVER_CRITICAL_PCT = new BigDecimal("95");
    private static final BigDecimal OPEN_ORDER_WARN_PCT = new BigDecimal("80");
    private static final BigDecimal OPEN_ORDER_CRITICAL_PCT = new BigDecimal("100");
    private static final BigDecimal LOSS_WARN_PCT = new BigDecimal("1");
    private static final BigDecimal LOSS_CRITICAL_PCT = new BigDecimal("3");
    private static final int SLA_CRITICAL_OPEN_MINUTES = 15;
    private static final int SLA_CRITICAL_IN_PROGRESS_MINUTES = 45;
    private static final int SLA_WARN_OPEN_MINUTES = 60;
    private static final int SLA_WARN_IN_PROGRESS_MINUTES = 240;
    private static final int SLA_INFO_OPEN_MINUTES = 240;
    private static final int SLA_INFO_IN_PROGRESS_MINUTES = 720;
    private static final Set<String> RISK_ALERT_WORKFLOW_STATUSES = Set.of("OPEN", "IN_PROGRESS", "RESOLVED");
    private static final int DEFAULT_STALE_MINUTES = 30;
    private static final String SYSTEM_ACTOR = "system@quant.local";

    private final AtomicLong orderSeq = new AtomicLong(1000);
    private final AtomicLong tradeSeq = new AtomicLong(5000);
    private final AtomicLong orderAuditSeq = new AtomicLong(8000);
    private final AtomicLong tradingControlAuditSeq = new AtomicLong(12000);
    private final AtomicLong playbookFeedbackSeq = new AtomicLong(16000);

    private final Map<Long, Order> orderStore = new ConcurrentHashMap<>();
    private final Map<Long, List<OrderAuditLog>> orderAuditStoreByOrder = new ConcurrentHashMap<>();
    private final Map<Long, List<Trade>> tradeStoreByOrder = new ConcurrentHashMap<>();
    private final Map<Long, Trade> tradeStoreById = new ConcurrentHashMap<>();
    private final Map<String, Position> positionStore = new ConcurrentHashMap<>();
    private final Map<Long, RiskLimit> riskLimitStore = new ConcurrentHashMap<>();
    private final Map<Long, List<TradingControlHistoryView>> tradingControlHistoryStoreByPortfolio = new ConcurrentHashMap<>();
    private final Map<String, RiskAlertAcknowledgementView> riskAlertAckStore = new ConcurrentHashMap<>();
    private final Map<Long, List<ProfitPlaybookActionFeedbackView>> playbookFeedbackStoreByPortfolio = new ConcurrentHashMap<>();

    public synchronized Order createOrder(Long portfolioId, String symbol, OrderSide side, BigDecimal quantity) {
        return createOrder(portfolioId, symbol, side, quantity, OrderType.MARKET, TimeInForce.DAY, null, SYSTEM_ACTOR);
    }

    public synchronized Order createOrder(
            Long portfolioId,
            String symbol,
            OrderSide side,
            BigDecimal quantity,
            OrderType orderType,
            TimeInForce timeInForce,
            BigDecimal limitPrice
    ) {
        return createOrder(
                portfolioId,
                symbol,
                side,
                quantity,
                orderType,
                timeInForce,
                limitPrice,
                SYSTEM_ACTOR
        );
    }

    public synchronized Order createOrder(
            Long portfolioId,
            String symbol,
            OrderSide side,
            BigDecimal quantity,
            OrderType orderType,
            TimeInForce timeInForce,
            BigDecimal limitPrice,
            String actor
    ) {
        requirePortfolioId(portfolioId);
        requireTradingEnabled(portfolioId);
        if (side == null) {
            throw new IllegalArgumentException("side is required");
        }

        OrderType normalizedOrderType = orderType == null ? OrderType.MARKET : orderType;
        TimeInForce normalizedTif = timeInForce == null ? TimeInForce.DAY : timeInForce;
        String normalizedSymbol = normalizeSymbol(symbol);
        BigDecimal normalizedQuantity = normalizeQuantity(quantity, "quantity");

        if (normalizedQuantity.compareTo(MAX_ORDER_QUANTITY) > 0) {
            throw new IllegalArgumentException("quantity exceeds max allowed: " + MAX_ORDER_QUANTITY);
        }

        BigDecimal normalizedLimitPrice = normalizeLimitPrice(normalizedOrderType, limitPrice);
        BigDecimal referencePrice = resolveReferencePrice(portfolioId, normalizedSymbol, normalizedOrderType, normalizedLimitPrice);
        BigDecimal estimatedNotional = scale6(normalizedQuantity.multiply(referencePrice));

        RiskLimit riskLimit = ensureRiskLimit(portfolioId);
        enforceOrderRiskLimit(riskLimit, portfolioId, normalizedSymbol, side, normalizedQuantity, referencePrice, estimatedNotional);

        if (side == OrderSide.SELL) {
            BigDecimal available = availableQuantityForSell(portfolioId, normalizedSymbol);
            if (normalizedQuantity.compareTo(available) > 0) {
                throw new IllegalArgumentException(
                        "sell quantity exceeds available position. symbol=" + normalizedSymbol
                                + ", available=" + available + ", requested=" + normalizedQuantity);
            }
        }

        Long orderId = orderSeq.incrementAndGet();
        Order order = new Order(
                orderId,
                portfolioId,
                normalizedSymbol,
                side,
                normalizedOrderType,
                normalizedTif,
                normalizedLimitPrice,
                normalizedQuantity,
                BigDecimal.ZERO,
                OrderStatus.NEW,
                Instant.now(),
                null,
                null
        );
        orderStore.put(orderId, order);
        appendOrderAudit(order, null, OrderStatus.NEW, "CREATE", "order created", actor);
        return order;
    }

    public synchronized Trade applyTrade(Long orderId, BigDecimal tradeQty, BigDecimal tradePrice) {
        return applyTrade(orderId, tradeQty, tradePrice, SYSTEM_ACTOR);
    }

    public synchronized Trade applyTrade(
            Long orderId,
            BigDecimal tradeQty,
            BigDecimal tradePrice,
            String actor
    ) {
        Order order = requireOrder(orderId);
        requireTradingEnabled(order.portfolioId());
        ensureTradeAllowed(order);

        BigDecimal normalizedTradeQty = normalizeQuantity(tradeQty, "tradeQuantity");
        BigDecimal normalizedTradePrice = normalizePrice(tradePrice);

        if (order.orderType() == OrderType.LIMIT && order.limitPrice() != null) {
            if (order.side() == OrderSide.BUY && normalizedTradePrice.compareTo(order.limitPrice()) > 0) {
                throw new IllegalArgumentException("buy limit order cannot execute above limitPrice");
            }
            if (order.side() == OrderSide.SELL && normalizedTradePrice.compareTo(order.limitPrice()) < 0) {
                throw new IllegalArgumentException("sell limit order cannot execute below limitPrice");
            }
        }

        BigDecimal remaining = order.quantity().subtract(order.filledQuantity());
        if (normalizedTradeQty.compareTo(remaining) > 0) {
            throw new IllegalArgumentException(
                    "trade quantity exceeds remaining order quantity. remaining=" + remaining
                            + ", requested=" + normalizedTradeQty);
        }

        if (order.side() == OrderSide.SELL) {
            BigDecimal currentQty = currentPositionQty(order.portfolioId(), order.symbol());
            if (normalizedTradeQty.compareTo(currentQty) > 0) {
                throw new IllegalArgumentException(
                        "sell trade exceeds current position. symbol=" + order.symbol()
                                + ", position=" + currentQty + ", trade=" + normalizedTradeQty);
            }
        }

        RiskLimit riskLimit = ensureRiskLimit(order.portfolioId());
        BigDecimal notional = scale6(normalizedTradeQty.multiply(normalizedTradePrice));
        BigDecimal projectedTurnover = todayTurnoverNotional(order.portfolioId()).add(notional);

        if (projectedTurnover.compareTo(riskLimit.maxDailyTurnover()) > 0) {
            throw new IllegalArgumentException(
                    "daily turnover limit exceeded. limit=" + riskLimit.maxDailyTurnover()
                            + ", projected=" + projectedTurnover);
        }

        BigDecimal fee = bpsAmount(notional, riskLimit.commissionBps());
        BigDecimal slippage = bpsAmount(notional, riskLimit.slippageBps());
        BigDecimal netCashFlow = order.side() == OrderSide.BUY
                ? notional.add(fee).add(slippage).negate()
                : notional.subtract(fee).subtract(slippage);

        Long tradeId = tradeSeq.incrementAndGet();
        Trade trade = new Trade(
                tradeId,
                orderId,
                order.symbol(),
                order.side(),
                normalizedTradeQty,
                normalizedTradePrice,
                notional,
                fee,
                slippage,
                scale6(netCashFlow),
                Instant.now()
        );

        tradeStoreByOrder.computeIfAbsent(orderId, k -> new ArrayList<>()).add(trade);
        tradeStoreById.put(tradeId, trade);

        BigDecimal newFilled = order.filledQuantity().add(normalizedTradeQty);
        OrderStatus newStatus = newFilled.compareTo(order.quantity()) >= 0 ? OrderStatus.FILLED : OrderStatus.PARTIAL;
        Order updatedOrder = order.withFill(newFilled, newStatus);
        orderStore.put(orderId, updatedOrder);
        appendOrderAudit(
                updatedOrder,
                order.status(),
                updatedOrder.status(),
                "TRADE_APPLIED",
                "tradeId=" + trade.tradeId() + ", qty=" + normalizedTradeQty + ", price=" + normalizedTradePrice,
                actor
        );

        recalculatePosition(order.portfolioId(), order.symbol());
        return trade;
    }

    public List<Position> getPositions(Long portfolioId) {
        return positionStore.values().stream()
                .filter(p -> Objects.equals(p.portfolioId(), portfolioId))
                .sorted(Comparator.comparing(Position::symbol))
                .toList();
    }

    public List<Order> getOrders() {
        return orderStore.values().stream()
                .sorted(Comparator.comparing(Order::orderId))
                .toList();
    }

    public List<Order> searchOrders(Long portfolioId, String symbol, String status) {
        return searchOrders(
                portfolioId,
                symbol,
                status,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public List<Order> searchOrders(
            Long portfolioId,
            String symbol,
            String status,
            String side,
            String orderType,
            String timeInForce,
            BigDecimal minQuantity,
            BigDecimal maxQuantity,
            BigDecimal minRemainingQuantity,
            BigDecimal maxRemainingQuantity,
            Instant createdFrom,
            Instant createdTo
    ) {
        validateQuantityRange(minQuantity, maxQuantity, "quantity");
        validateQuantityRange(minRemainingQuantity, maxRemainingQuantity, "remainingQuantity");
        validateInstantRange(createdFrom, createdTo, "createdAt");

        String normalizedSymbol = normalizeFilterString(symbol);
        String normalizedStatus = normalizeFilterString(status);
        String normalizedSide = normalizeFilterString(side);
        String normalizedOrderType = normalizeFilterString(orderType);
        String normalizedTif = normalizeFilterString(timeInForce);

        return orderStore.values().stream()
                .filter(o -> portfolioId == null || Objects.equals(o.portfolioId(), portfolioId))
                .filter(o -> normalizedSymbol == null || o.symbol().equalsIgnoreCase(normalizedSymbol))
                .filter(o -> normalizedStatus == null || o.status().name().equalsIgnoreCase(normalizedStatus))
                .filter(o -> normalizedSide == null || o.side().name().equalsIgnoreCase(normalizedSide))
                .filter(o -> normalizedOrderType == null || o.orderType().name().equalsIgnoreCase(normalizedOrderType))
                .filter(o -> normalizedTif == null || o.timeInForce().name().equalsIgnoreCase(normalizedTif))
                .filter(o -> minQuantity == null || o.quantity().compareTo(minQuantity) >= 0)
                .filter(o -> maxQuantity == null || o.quantity().compareTo(maxQuantity) <= 0)
                .filter(o -> minRemainingQuantity == null || remainingQuantity(o).compareTo(minRemainingQuantity) >= 0)
                .filter(o -> maxRemainingQuantity == null || remainingQuantity(o).compareTo(maxRemainingQuantity) <= 0)
                .filter(o -> createdFrom == null || !o.createdAt().isBefore(createdFrom))
                .filter(o -> createdTo == null || !o.createdAt().isAfter(createdTo))
                .sorted(Comparator.comparing(Order::orderId))
                .toList();
    }

    public OrderInsightView getOrderInsight(Long orderId, Integer staleMinutes) {
        int staleThresholdMinutes = staleMinutes == null ? DEFAULT_STALE_MINUTES : staleMinutes;
        if (staleThresholdMinutes < 0) {
            throw new IllegalArgumentException("staleMinutes must be >= 0");
        }

        Order order = requireOrder(orderId);
        Instant now = Instant.now();
        BigDecimal remainingQuantity = scale6(order.quantity().subtract(order.filledQuantity()));
        BigDecimal fillRatePct = order.quantity().compareTo(BigDecimal.ZERO) > 0
                ? scale6(order.filledQuantity()
                        .divide(order.quantity(), 6, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100")))
                : BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);

        BigDecimal referencePrice = resolveReferencePrice(
                order.portfolioId(),
                order.symbol(),
                order.orderType(),
                order.limitPrice()
        );
        BigDecimal requestedNotional = scale6(order.quantity().multiply(referencePrice));

        List<Trade> trades = searchTrades(order.portfolioId(), orderId, null);
        List<OrderAuditLog> audits = searchOrderAudits(orderId, order.portfolioId(), order.symbol(), null, null);

        BigDecimal executedNotional = trades.stream()
                .map(Trade::notional)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal executedQuantity = trades.stream()
                .map(Trade::tradeQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageExecutionPrice = executedQuantity.compareTo(BigDecimal.ZERO) > 0
                ? scale6(executedNotional.divide(executedQuantity, 6, RoundingMode.HALF_UP))
                : BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);
        BigDecimal totalFee = trades.stream()
                .map(Trade::fee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalSlippage = trades.stream()
                .map(Trade::slippage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netCashFlow = trades.stream()
                .map(Trade::netCashFlow)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<RiskAlertView> riskSignals = new ArrayList<>(searchRiskAlerts(order.portfolioId()).stream()
                .filter(alert -> Objects.equals(alert.portfolioId(), order.portfolioId()))
                .filter(alert -> !"INFO".equalsIgnoreCase(alert.severity()))
                .toList());

        List<OrderHealthView> healthRows = searchOrderHealth(order.portfolioId(), order.symbol(), staleThresholdMinutes);
        if (!healthRows.isEmpty()) {
            OrderHealthView health = healthRows.get(0);
            if (!"HEALTHY".equalsIgnoreCase(health.healthStatus())) {
                String severity = "CRITICAL".equalsIgnoreCase(health.healthStatus()) ? "CRITICAL" : "WARN";
                riskSignals.add(new RiskAlertView(
                        "ORDER_HEALTH_" + orderId,
                        order.portfolioId(),
                        severity,
                        "ORDER_HEALTH_" + health.healthStatus(),
                        health.healthNote(),
                        "staleOrderCount",
                        BigDecimal.valueOf(health.staleOrderCount()),
                        BigDecimal.valueOf(health.staleThresholdMinutes()),
                        now
                ));
            }
        }

        List<RiskAlertView> sortedRiskSignals = riskSignals.stream()
                .sorted(Comparator.comparing((RiskAlertView alert) -> severityRank(alert.severity()))
                        .thenComparing(RiskAlertView::code))
                .toList();

        long ageMinutes = orderAgeMinutes(order.createdAt(), now);
        boolean cancelable = isOpenOrderStatus(order.status());
        boolean rejectable = (order.status() == OrderStatus.NEW || order.status() == OrderStatus.SENT)
                && order.filledQuantity().compareTo(BigDecimal.ZERO) <= 0;

        return new OrderInsightView(
                order,
                remainingQuantity,
                fillRatePct,
                requestedNotional,
                scale6(executedNotional),
                averageExecutionPrice,
                scale6(totalFee),
                scale6(totalSlippage),
                scale6(netCashFlow),
                ageMinutes,
                cancelable,
                rejectable,
                trades,
                audits,
                sortedRiskSignals
        );
    }

    public synchronized Order deleteOrder(Long orderId) {
        return deleteOrder(orderId, SYSTEM_ACTOR);
    }

    public synchronized Order deleteOrder(Long orderId, String actor) {
        Order order = requireOrder(orderId);
        if (order.filledQuantity().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalArgumentException("cannot delete partially/fully filled order: " + orderId);
        }

        List<Trade> trades = tradeStoreByOrder.getOrDefault(orderId, List.of());
        if (!trades.isEmpty()) {
            throw new IllegalArgumentException("cannot delete order with executed trades: " + orderId);
        }

        appendOrderAudit(order, order.status(), null, "DELETE", "order deleted", actor);
        orderStore.remove(orderId);
        tradeStoreByOrder.remove(orderId);
        return order;
    }

    public synchronized Order cancelOrder(Long orderId) {
        return cancelOrder(orderId, "manual cancel", SYSTEM_ACTOR);
    }

    public synchronized Order cancelOrder(Long orderId, String reason) {
        return cancelOrder(orderId, reason, SYSTEM_ACTOR);
    }

    public synchronized Order cancelOrder(Long orderId, String reason, String actor) {
        Order order = requireOrder(orderId);
        if (order.status() == OrderStatus.CANCELED) {
            return order;
        }
        if (order.status() == OrderStatus.REJECTED || order.status() == OrderStatus.FILLED) {
            throw new IllegalArgumentException("cannot cancel order in status: " + order.status());
        }

        if (!isOpenOrderStatus(order.status())) {
            throw new IllegalArgumentException("order is not cancelable in status: " + order.status());
        }

        String normalizedReason = reason == null || reason.isBlank() ? "manual cancel" : reason.trim();
        Order canceled = order.withDecision(OrderStatus.CANCELED, normalizedReason, Instant.now());
        orderStore.put(orderId, canceled);
        appendOrderAudit(canceled, order.status(), canceled.status(), "CANCEL", normalizedReason, actor);
        return canceled;
    }

    public synchronized Order rejectOrder(Long orderId, String reason) {
        return rejectOrder(orderId, reason, SYSTEM_ACTOR);
    }

    public synchronized Order rejectOrder(Long orderId, String reason, String actor) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reject reason is required");
        }

        Order order = requireOrder(orderId);
        if (order.status() == OrderStatus.REJECTED) {
            return order;
        }
        if (order.status() == OrderStatus.CANCELED || order.status() == OrderStatus.FILLED) {
            throw new IllegalArgumentException("cannot reject order in status: " + order.status());
        }

        if (order.filledQuantity().compareTo(BigDecimal.ZERO) > 0 || order.status() == OrderStatus.PARTIAL) {
            throw new IllegalArgumentException("cannot reject partially executed order: " + order.orderId());
        }

        if (!(order.status() == OrderStatus.NEW || order.status() == OrderStatus.SENT)) {
            throw new IllegalArgumentException("order is not rejectable in status: " + order.status());
        }

        Order rejected = order.withDecision(OrderStatus.REJECTED, reason.trim(), Instant.now());
        orderStore.put(orderId, rejected);
        appendOrderAudit(rejected, order.status(), rejected.status(), "REJECT", reason.trim(), actor);
        return rejected;
    }

    public List<OrderAuditLog> searchOrderAudits(
            Long orderId,
            Long portfolioId,
            String symbol,
            String action
    ) {
        return searchOrderAudits(orderId, portfolioId, symbol, action, null);
    }

    public List<OrderAuditLog> searchOrderAudits(
            Long orderId,
            Long portfolioId,
            String symbol,
            String action,
            String actor
    ) {
        String normalizedSymbol = symbol == null ? null : symbol.trim();
        String normalizedAction = action == null ? null : action.trim();
        String normalizedActor = actor == null ? null : actor.trim();

        return orderAuditStoreByOrder.values().stream()
                .flatMap(List::stream)
                .filter(log -> orderId == null || Objects.equals(log.orderId(), orderId))
                .filter(log -> portfolioId == null || Objects.equals(log.portfolioId(), portfolioId))
                .filter(log -> normalizedSymbol == null || normalizedSymbol.isBlank() || log.symbol().equalsIgnoreCase(normalizedSymbol))
                .filter(log -> normalizedAction == null || normalizedAction.isBlank() || log.action().equalsIgnoreCase(normalizedAction))
                .filter(log -> normalizedActor == null || normalizedActor.isBlank() || log.actor().equalsIgnoreCase(normalizedActor))
                .sorted(Comparator.comparing(OrderAuditLog::actedAt).thenComparing(OrderAuditLog::auditId))
                .toList();
    }

    public OrderAuditSummaryView summarizeOrderAudits(
            Long orderId,
            Long portfolioId,
            String symbol,
            String action,
            String actor,
            Integer recentMinutes
    ) {
        int normalizedRecentMinutes = normalizeRecentMinutes(recentMinutes);
        Instant generatedAt = Instant.now();
        Instant recentThreshold = generatedAt.minus(Duration.ofMinutes(normalizedRecentMinutes));

        List<OrderAuditLog> audits = searchOrderAudits(orderId, portfolioId, symbol, action, actor);
        Map<String, Long> actionCounterMap = new HashMap<>();
        Map<String, Long> actorCounterMap = new HashMap<>();
        Map<String, Long> transitionCounterMap = new HashMap<>();
        Map<String, String> transitionFromMap = new HashMap<>();
        Map<String, String> transitionToMap = new HashMap<>();
        Set<Long> distinctOrderIds = new TreeSet<>();

        long recentCount = 0L;
        Instant lastActedAt = null;

        for (OrderAuditLog audit : audits) {
            if (audit.orderId() != null) {
                distinctOrderIds.add(audit.orderId());
            }

            String actionKey = normalizeAuditAction(audit.action());
            actionCounterMap.merge(actionKey, 1L, Long::sum);

            String actorKey = normalizeAuditActor(audit.actor());
            actorCounterMap.merge(actorKey, 1L, Long::sum);

            String fromStatus = normalizeAuditStatus(audit.fromStatus());
            String toStatus = normalizeAuditStatus(audit.toStatus());
            String transitionKey = fromStatus + "->" + toStatus;
            transitionCounterMap.merge(transitionKey, 1L, Long::sum);
            transitionFromMap.putIfAbsent(transitionKey, fromStatus);
            transitionToMap.putIfAbsent(transitionKey, toStatus);

            Instant actedAt = audit.actedAt();
            if (actedAt != null && !actedAt.isBefore(recentThreshold)) {
                recentCount += 1;
            }
            if (actedAt != null && (lastActedAt == null || actedAt.isAfter(lastActedAt))) {
                lastActedAt = actedAt;
            }
        }

        List<OrderAuditActionCounterView> actionCounters = actionCounterMap.entrySet().stream()
                .map(entry -> new OrderAuditActionCounterView(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(OrderAuditActionCounterView::count, Comparator.reverseOrder())
                        .thenComparing(OrderAuditActionCounterView::action))
                .toList();

        List<OrderAuditTransitionCounterView> transitionCounters = transitionCounterMap.entrySet().stream()
                .map(entry -> new OrderAuditTransitionCounterView(
                        transitionFromMap.getOrDefault(entry.getKey(), "-"),
                        transitionToMap.getOrDefault(entry.getKey(), "-"),
                        entry.getValue()))
                .sorted(Comparator.comparing(OrderAuditTransitionCounterView::count, Comparator.reverseOrder())
                        .thenComparing(OrderAuditTransitionCounterView::fromStatus)
                        .thenComparing(OrderAuditTransitionCounterView::toStatus))
                .toList();

        List<OrderAuditActorCounterView> topActors = actorCounterMap.entrySet().stream()
                .map(entry -> new OrderAuditActorCounterView(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(OrderAuditActorCounterView::count, Comparator.reverseOrder())
                        .thenComparing(OrderAuditActorCounterView::actor))
                .limit(7)
                .toList();

        return new OrderAuditSummaryView(
                portfolioId,
                normalizedRecentMinutes,
                (long) audits.size(),
                recentCount,
                (long) distinctOrderIds.size(),
                lastActedAt,
                generatedAt,
                actionCounters,
                transitionCounters,
                topActors
        );
    }

    public List<Trade> getTrades() {
        return tradeStoreById.values().stream()
                .sorted(Comparator.comparing(Trade::tradeId))
                .toList();
    }

    public List<Trade> searchTrades(Long orderId, String symbol) {
        return searchTrades(null, orderId, symbol);
    }

    public List<Trade> searchTrades(Long portfolioId, Long orderId, String symbol) {
        return tradeStoreById.values().stream()
                .filter(t -> orderId == null || Objects.equals(t.orderId(), orderId))
                .filter(t -> symbol == null || symbol.isBlank() || t.symbol().equalsIgnoreCase(symbol.trim()))
                .filter(t -> {
                    if (portfolioId == null) {
                        return true;
                    }
                    Order order = orderStore.get(t.orderId());
                    return order != null && Objects.equals(order.portfolioId(), portfolioId);
                })
                .sorted(Comparator.comparing(Trade::tradeId))
                .toList();
    }

    public TradeExecutionView requireTradeExecutionView(Long tradeId) {
        Trade trade = tradeStoreById.get(tradeId);
        if (trade == null) {
            throw new IllegalArgumentException("trade not found: " + tradeId);
        }
        Order order = requireOrder(trade.orderId());
        return new TradeExecutionView(order.portfolioId(), trade);
    }

    public record TradeExecutionView(Long portfolioId, Trade trade) {
    }

    public RiskLimit upsertRiskLimit(
            Long portfolioId,
            BigDecimal maxOrderNotional,
            BigDecimal maxPositionNotionalPerSymbol,
            BigDecimal maxDailyTurnover,
            Integer maxOpenOrdersPerSymbol,
            BigDecimal commissionBps,
            BigDecimal slippageBps
    ) {
        requirePortfolioId(portfolioId);
        RiskLimit current = ensureRiskLimit(portfolioId);
        RiskLimit updated = new RiskLimit(
                portfolioId,
                requirePositive(maxOrderNotional, "maxOrderNotional"),
                requirePositive(maxPositionNotionalPerSymbol, "maxPositionNotionalPerSymbol"),
                requirePositive(maxDailyTurnover, "maxDailyTurnover"),
                requirePositiveInt(maxOpenOrdersPerSymbol, "maxOpenOrdersPerSymbol"),
                requireNonNegative(commissionBps, "commissionBps"),
                requireNonNegative(slippageBps, "slippageBps"),
                current.tradingEnabled(),
                current.killSwitchReason(),
                current.killSwitchUpdatedAt(),
                current.killSwitchUpdatedBy()
        );
        riskLimitStore.put(portfolioId, updated);
        return updated;
    }

    public synchronized RiskLimit updateTradingControl(
            Long portfolioId,
            Boolean tradingEnabled,
            String reason,
            String actor
    ) {
        requirePortfolioId(portfolioId);
        if (tradingEnabled == null) {
            throw new IllegalArgumentException("tradingEnabled is required");
        }

        RiskLimit current = ensureRiskLimit(portfolioId);
        boolean enabled = Boolean.TRUE.equals(tradingEnabled);
        String normalizedReason = reason == null ? null : reason.trim();
        if (!enabled && (normalizedReason == null || normalizedReason.isBlank())) {
            throw new IllegalArgumentException("kill switch reason is required when disabling trading");
        }
        if (enabled && (normalizedReason == null || normalizedReason.isBlank())) {
            normalizedReason = "manual resume";
        }
        String normalizedActor = normalizeActor(actor);
        Instant changedAt = Instant.now();

        RiskLimit updated = new RiskLimit(
                current.portfolioId(),
                current.maxOrderNotional(),
                current.maxPositionNotionalPerSymbol(),
                current.maxDailyTurnover(),
                current.maxOpenOrdersPerSymbol(),
                current.commissionBps(),
                current.slippageBps(),
                enabled,
                enabled ? null : normalizedReason,
                changedAt,
                normalizedActor
        );

        riskLimitStore.put(portfolioId, updated);
        appendTradingControlHistory(current, updated, normalizedReason, changedAt, normalizedActor);
        return updated;
    }

    public List<RiskLimit> searchTradingControls(Long portfolioId) {
        return searchRiskLimits(portfolioId);
    }

    public List<TradingControlHistoryView> searchTradingControlHistory(Long portfolioId, Integer limit) {
        int normalizedLimit = normalizeTradingControlHistoryLimit(limit);
        return tradingControlHistoryStoreByPortfolio.values().stream()
                .flatMap(List::stream)
                .filter(item -> portfolioId == null || Objects.equals(item.portfolioId(), portfolioId))
                .sorted(Comparator.comparing(TradingControlHistoryView::updatedAt, Comparator.reverseOrder())
                        .thenComparing(TradingControlHistoryView::historyId, Comparator.reverseOrder()))
                .limit(normalizedLimit)
                .toList();
    }

    public synchronized TradingResumeResult resumeTradingWithGuards(
            Long portfolioId,
            String reason,
            String actor,
            Boolean force
    ) {
        requirePortfolioId(portfolioId);
        String normalizedReason = reason == null ? "" : reason.trim();
        if (normalizedReason.isBlank()) {
            throw new IllegalArgumentException("resume reason is required");
        }
        boolean forceResume = Boolean.TRUE.equals(force);

        List<RiskAlertView> blockingAlerts = searchRiskAlerts(portfolioId).stream()
                .filter(alert -> "CRITICAL".equalsIgnoreCase(alert.severity()))
                .filter(alert -> !"TRADING_HALTED".equalsIgnoreCase(alert.code()))
                .toList();

        if (!blockingAlerts.isEmpty() && !forceResume) {
            List<String> blockedCodes = blockingAlerts.stream()
                    .map(RiskAlertView::code)
                    .distinct()
                    .toList();
            List<String> blockedMessages = blockingAlerts.stream()
                    .map(alert -> alert.code() + ": " + alert.message())
                    .toList();
            return new TradingResumeResult(
                    portfolioId,
                    false,
                    false,
                    blockingAlerts.size(),
                    blockedCodes,
                    blockedMessages,
                    Instant.now(),
                    normalizeActor(actor)
            );
        }

        RiskLimit updated = updateTradingControl(
                portfolioId,
                true,
                "resume: " + normalizedReason,
                actor
        );

        return new TradingResumeResult(
                updated.portfolioId(),
                updated.tradingEnabled(),
                true,
                0,
                List.of(),
                List.of(),
                updated.killSwitchUpdatedAt(),
                updated.killSwitchUpdatedBy()
        );
    }

    public RiskAlertView getRiskAlert(Long portfolioId, String alertKey) {
        requirePortfolioId(portfolioId);
        String normalizedAlertKey = normalizeRiskAlertKey(alertKey);
        return searchRiskAlerts(portfolioId).stream()
                .filter(alert -> alert.alertKey().equalsIgnoreCase(normalizedAlertKey))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "active risk alert not found. portfolioId=" + portfolioId + ", alertKey=" + normalizedAlertKey));
    }

    public synchronized RiskAlertAcknowledgementView acknowledgeRiskAlert(
            Long portfolioId,
            String alertKey,
            String note,
            String actor
    ) {
        RiskAlertView alert = getRiskAlert(portfolioId, alertKey);
        if ("INFO".equalsIgnoreCase(alert.severity())) {
            throw new IllegalArgumentException("info alert cannot be acknowledged");
        }

        RiskAlertAcknowledgementView current = getRiskAlertAcknowledgement(alert.portfolioId(), alert.alertKey());
        String normalizedNote = normalizeRiskAlertNote(note);
        String normalizedActor = normalizeActor(actor);
        String workflowStatus = normalizeRiskAlertWorkflowStatus(current.workflowStatus());
        Instant now = Instant.now();

        RiskAlertAcknowledgementView updated = new RiskAlertAcknowledgementView(
                alert.alertKey(),
                alert.portfolioId(),
                alert.code(),
                true,
                normalizedNote,
                normalizedActor,
                now,
                workflowStatus,
                current.assignee(),
                current.resolvedBy(),
                current.resolvedAt(),
                normalizedActor,
                now
        );
        riskAlertAckStore.put(riskAlertAckKey(alert.portfolioId(), alert.alertKey()), updated);
        return updated;
    }

    public synchronized RiskAlertAcknowledgementView clearRiskAlertAcknowledgement(
            Long portfolioId,
            String alertKey,
            String actor
    ) {
        RiskAlertView alert = getRiskAlert(portfolioId, alertKey);
        String normalizedActor = normalizeActor(actor);
        Instant now = Instant.now();
        RiskAlertAcknowledgementView updated = new RiskAlertAcknowledgementView(
                alert.alertKey(),
                alert.portfolioId(),
                alert.code(),
                false,
                null,
                null,
                null,
                "OPEN",
                null,
                null,
                null,
                normalizedActor,
                now
        );
        riskAlertAckStore.remove(riskAlertAckKey(alert.portfolioId(), alert.alertKey()));
        return updated;
    }

    public synchronized RiskAlertAcknowledgementView updateRiskAlertWorkflow(
            Long portfolioId,
            String alertKey,
            String workflowStatus,
            String note,
            String assignee,
            String actor
    ) {
        RiskAlertView alert = getRiskAlert(portfolioId, alertKey);
        if ("INFO".equalsIgnoreCase(alert.severity())) {
            throw new IllegalArgumentException("info alert does not support workflow transitions");
        }

        RiskAlertAcknowledgementView current = getRiskAlertAcknowledgement(alert.portfolioId(), alert.alertKey());
        String normalizedStatus = normalizeRiskAlertWorkflowStatus(workflowStatus);
        String normalizedNote = normalizeRiskAlertNote(note);
        String normalizedAssignee = normalizeRiskAlertAssignee(assignee);
        String normalizedActor = normalizeActor(actor);
        Instant now = Instant.now();

        if ("IN_PROGRESS".equals(normalizedStatus) && !Boolean.TRUE.equals(current.acknowledged())) {
            throw new IllegalArgumentException("risk alert must be acknowledged before setting IN_PROGRESS");
        }
        if ("RESOLVED".equals(normalizedStatus) && !Boolean.TRUE.equals(current.acknowledged())) {
            throw new IllegalArgumentException("risk alert must be acknowledged before setting RESOLVED");
        }
        if ("RESOLVED".equals(normalizedStatus) && (normalizedNote == null || normalizedNote.isBlank())) {
            throw new IllegalArgumentException("workflow note is required when setting RESOLVED");
        }

        RiskAlertAcknowledgementView updated = new RiskAlertAcknowledgementView(
                alert.alertKey(),
                alert.portfolioId(),
                alert.code(),
                current.acknowledged(),
                normalizedNote != null ? normalizedNote : current.note(),
                current.acknowledgedBy(),
                current.acknowledgedAt(),
                normalizedStatus,
                normalizedAssignee,
                "RESOLVED".equals(normalizedStatus) ? normalizedActor : null,
                "RESOLVED".equals(normalizedStatus) ? now : null,
                normalizedActor,
                now
        );

        riskAlertAckStore.put(riskAlertAckKey(alert.portfolioId(), alert.alertKey()), updated);
        return updated;
    }

    public RiskAlertAcknowledgementView getRiskAlertAcknowledgement(Long portfolioId, String alertKey) {
        requirePortfolioId(portfolioId);
        String normalizedAlertKey = normalizeRiskAlertKey(alertKey);
        RiskAlertAcknowledgementView view = riskAlertAckStore.get(riskAlertAckKey(portfolioId, normalizedAlertKey));
        if (view != null && Boolean.TRUE.equals(view.acknowledged())) {
            return view;
        }
        return new RiskAlertAcknowledgementView(
                normalizedAlertKey,
                portfolioId,
                null,
                false,
                null,
                null,
                null,
                "OPEN",
                null,
                null,
                null,
                null,
                null
        );
    }

    public List<RiskAlertView> searchRiskAlerts(Long portfolioId) {
        Instant now = Instant.now();
        List<RiskAlertView> alerts = new ArrayList<>();

        for (PortfolioSummary summary : searchPortfolioSummaries(portfolioId)) {
            Long scopedPortfolioId = summary.portfolioId();
            RiskLimit riskLimit = ensureRiskLimit(scopedPortfolioId);

            if (!Boolean.TRUE.equals(riskLimit.tradingEnabled())) {
                alerts.add(new RiskAlertView(
                        "KILL_SWITCH_" + scopedPortfolioId,
                        scopedPortfolioId,
                        "CRITICAL",
                        "TRADING_HALTED",
                        "거래가 Kill Switch로 중지되어 있습니다.",
                        "tradingEnabled",
                        BigDecimal.ZERO,
                        BigDecimal.ONE,
                        riskLimit.killSwitchUpdatedAt() == null ? now : riskLimit.killSwitchUpdatedAt()
                ));
            }

            BigDecimal turnoverUsage = summary.turnoverUsagePct() == null ? BigDecimal.ZERO : summary.turnoverUsagePct();
            if (turnoverUsage.compareTo(TURNOVER_CRITICAL_PCT) >= 0) {
                alerts.add(new RiskAlertView(
                        "TURNOVER_CRIT_" + scopedPortfolioId,
                        scopedPortfolioId,
                        "CRITICAL",
                        "DAILY_TURNOVER_CRITICAL",
                        "일일 회전율 한도 사용률이 임계치(95%)를 초과했습니다.",
                        "turnoverUsagePct",
                        turnoverUsage,
                        TURNOVER_CRITICAL_PCT,
                        now
                ));
            } else if (turnoverUsage.compareTo(TURNOVER_WARN_PCT) >= 0) {
                alerts.add(new RiskAlertView(
                        "TURNOVER_WARN_" + scopedPortfolioId,
                        scopedPortfolioId,
                        "WARN",
                        "DAILY_TURNOVER_WARN",
                        "일일 회전율 한도 사용률이 경고치(80%)를 초과했습니다.",
                        "turnoverUsagePct",
                        turnoverUsage,
                        TURNOVER_WARN_PCT,
                        now
                ));
            }

            BigDecimal openOrderUsagePct = BigDecimal.ZERO;
            if (riskLimit.maxOpenOrdersPerSymbol() != null && riskLimit.maxOpenOrdersPerSymbol() > 0) {
                openOrderUsagePct = BigDecimal.valueOf(summary.openOrderCount())
                        .divide(BigDecimal.valueOf(riskLimit.maxOpenOrdersPerSymbol()), 6, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
            }

            if (openOrderUsagePct.compareTo(OPEN_ORDER_CRITICAL_PCT) >= 0) {
                alerts.add(new RiskAlertView(
                        "OPEN_ORDER_CRIT_" + scopedPortfolioId,
                        scopedPortfolioId,
                        "CRITICAL",
                        "OPEN_ORDER_CRITICAL",
                        "오픈 주문 수가 종목별 한도 대비 100% 이상입니다.",
                        "openOrderUsagePct",
                        scale6(openOrderUsagePct),
                        OPEN_ORDER_CRITICAL_PCT,
                        now
                ));
            } else if (openOrderUsagePct.compareTo(OPEN_ORDER_WARN_PCT) >= 0) {
                alerts.add(new RiskAlertView(
                        "OPEN_ORDER_WARN_" + scopedPortfolioId,
                        scopedPortfolioId,
                        "WARN",
                        "OPEN_ORDER_WARN",
                        "오픈 주문 수가 종목별 한도 대비 80% 이상입니다.",
                        "openOrderUsagePct",
                        scale6(openOrderUsagePct),
                        OPEN_ORDER_WARN_PCT,
                        now
                ));
            }

            BigDecimal marketAbs = summary.marketValue().abs();
            if (marketAbs.compareTo(BigDecimal.ZERO) > 0 && summary.totalPnl().compareTo(BigDecimal.ZERO) < 0) {
                BigDecimal lossPct = summary.totalPnl()
                        .abs()
                        .divide(marketAbs, 6, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));

                if (lossPct.compareTo(LOSS_CRITICAL_PCT) >= 0) {
                    alerts.add(new RiskAlertView(
                            "LOSS_CRIT_" + scopedPortfolioId,
                            scopedPortfolioId,
                            "CRITICAL",
                            "PNL_DRAWDOWN_CRITICAL",
                            "포트폴리오 손실률이 임계치(3%)를 초과했습니다.",
                            "lossPct",
                            scale6(lossPct),
                            LOSS_CRITICAL_PCT,
                            now
                    ));
                } else if (lossPct.compareTo(LOSS_WARN_PCT) >= 0) {
                    alerts.add(new RiskAlertView(
                            "LOSS_WARN_" + scopedPortfolioId,
                            scopedPortfolioId,
                            "WARN",
                            "PNL_DRAWDOWN_WARN",
                            "포트폴리오 손실률이 경고치(1%)를 초과했습니다.",
                            "lossPct",
                            scale6(lossPct),
                            LOSS_WARN_PCT,
                            now
                    ));
                }
            }

            boolean hasAlert = alerts.stream().anyMatch(alert -> Objects.equals(alert.portfolioId(), scopedPortfolioId));
            if (!hasAlert) {
                alerts.add(new RiskAlertView(
                        "RISK_OK_" + scopedPortfolioId,
                        scopedPortfolioId,
                        "INFO",
                        "RISK_NORMAL",
                        "현재 임계치 기반 리스크 경보가 없습니다.",
                        "riskStatus",
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        now
                ));
            }
        }

        return alerts.stream()
                .sorted(Comparator.comparing(RiskAlertView::portfolioId)
                        .thenComparing(alert -> severityRank(alert.severity()))
                .thenComparing(RiskAlertView::code))
                .toList();
    }

    public RiskAlertOperationalView evaluateRiskAlertOperationalState(RiskAlertView alert) {
        RiskAlertAcknowledgementView ack = getRiskAlertAcknowledgement(alert.portfolioId(), alert.alertKey());
        Instant now = Instant.now();
        long ageMinutes = alertAgeMinutes(alert.occurredAt(), now);
        int slaTargetMinutes = resolveRiskAlertSlaTargetMinutes(alert.severity(), ack.workflowStatus());
        boolean slaBreached = isRiskAlertSlaBreached(ageMinutes, slaTargetMinutes, ack.workflowStatus());
        int priorityScore = resolveRiskAlertPriorityScore(alert.severity(), ack.workflowStatus(), ack.acknowledged(), slaBreached, ageMinutes);
        return new RiskAlertOperationalView(ageMinutes, slaTargetMinutes, slaBreached, priorityScore);
    }

    public List<RiskAlertOverviewView> searchRiskAlertOverviews(Long portfolioId) {
        Instant now = Instant.now();
        Map<Long, List<RiskAlertView>> groupedByPortfolio = new HashMap<>();
        for (RiskAlertView alert : searchRiskAlerts(portfolioId)) {
            groupedByPortfolio.computeIfAbsent(alert.portfolioId(), key -> new ArrayList<>()).add(alert);
        }

        List<RiskAlertOverviewView> rows = new ArrayList<>();
        for (Map.Entry<Long, List<RiskAlertView>> entry : groupedByPortfolio.entrySet()) {
            Long scopedPortfolioId = entry.getKey();
            List<RiskAlertView> alerts = entry.getValue();

            long criticalCount = 0L;
            long warnCount = 0L;
            long infoCount = 0L;
            long unacknowledgedCount = 0L;
            long openCount = 0L;
            long inProgressCount = 0L;
            long resolvedCount = 0L;
            long slaBreachedCount = 0L;
            long oldestOpenAgeMinutes = 0L;

            long ackMinutesTotal = 0L;
            long ackMinutesCount = 0L;
            long resolveMinutesTotal = 0L;
            long resolveMinutesCount = 0L;

            for (RiskAlertView alert : alerts) {
                String severity = alert.severity() == null ? "INFO" : alert.severity().toUpperCase(Locale.ROOT);
                if ("CRITICAL".equals(severity)) {
                    criticalCount += 1;
                } else if ("WARN".equals(severity)) {
                    warnCount += 1;
                } else {
                    infoCount += 1;
                }

                RiskAlertAcknowledgementView ack = getRiskAlertAcknowledgement(alert.portfolioId(), alert.alertKey());
                String workflowStatus = normalizeRiskAlertWorkflowStatus(ack.workflowStatus());
                boolean acknowledged = Boolean.TRUE.equals(ack.acknowledged());
                long ageMinutes = alertAgeMinutes(alert.occurredAt(), now);
                int slaTargetMinutes = resolveRiskAlertSlaTargetMinutes(severity, workflowStatus);
                boolean slaBreached = isRiskAlertSlaBreached(ageMinutes, slaTargetMinutes, workflowStatus);

                if (!acknowledged) {
                    unacknowledgedCount += 1;
                }
                if ("OPEN".equals(workflowStatus)) {
                    openCount += 1;
                } else if ("IN_PROGRESS".equals(workflowStatus)) {
                    inProgressCount += 1;
                } else if ("RESOLVED".equals(workflowStatus)) {
                    resolvedCount += 1;
                }
                if (slaBreached) {
                    slaBreachedCount += 1;
                }
                if (!"RESOLVED".equals(workflowStatus) && ageMinutes > oldestOpenAgeMinutes) {
                    oldestOpenAgeMinutes = ageMinutes;
                }

                if (ack.acknowledgedAt() != null && alert.occurredAt() != null && !ack.acknowledgedAt().isBefore(alert.occurredAt())) {
                    ackMinutesTotal += Duration.between(alert.occurredAt(), ack.acknowledgedAt()).toMinutes();
                    ackMinutesCount += 1;
                }
                if (ack.resolvedAt() != null && alert.occurredAt() != null && !ack.resolvedAt().isBefore(alert.occurredAt())) {
                    resolveMinutesTotal += Duration.between(alert.occurredAt(), ack.resolvedAt()).toMinutes();
                    resolveMinutesCount += 1;
                }
            }

            Long avgAckMinutes = ackMinutesCount > 0 ? Math.round((double) ackMinutesTotal / (double) ackMinutesCount) : null;
            Long avgResolveMinutes = resolveMinutesCount > 0 ? Math.round((double) resolveMinutesTotal / (double) resolveMinutesCount) : null;

            rows.add(new RiskAlertOverviewView(
                    scopedPortfolioId,
                    (long) alerts.size(),
                    criticalCount,
                    warnCount,
                    infoCount,
                    unacknowledgedCount,
                    openCount,
                    inProgressCount,
                    resolvedCount,
                    slaBreachedCount,
                    oldestOpenAgeMinutes,
                    avgAckMinutes,
                    avgResolveMinutes,
                    now
            ));
        }

        return rows.stream()
                .sorted(Comparator.comparing(RiskAlertOverviewView::portfolioId))
                .toList();
    }

    public List<ExecutionQualityView> searchExecutionQualities(Long portfolioId, String symbol) {
        String normalizedSymbol = null;
        if (symbol != null && !symbol.isBlank()) {
            normalizedSymbol = normalizeSymbol(symbol);
        }

        Map<String, List<Order>> groupedOrders = new HashMap<>();
        for (Order order : orderStore.values()) {
            if (portfolioId != null && !Objects.equals(order.portfolioId(), portfolioId)) {
                continue;
            }
            if (normalizedSymbol != null && !order.symbol().equalsIgnoreCase(normalizedSymbol)) {
                continue;
            }
            groupedOrders.computeIfAbsent(key(order.portfolioId(), order.symbol()), k -> new ArrayList<>()).add(order);
        }

        List<ExecutionQualityView> rows = new ArrayList<>();
        for (List<Order> orders : groupedOrders.values()) {
            if (orders.isEmpty()) {
                continue;
            }

            Order pivot = orders.get(0);
            Long scopedPortfolioId = pivot.portfolioId();
            String scopedSymbol = pivot.symbol().toUpperCase(Locale.ROOT);

            int orderCount = orders.size();
            int filledOrderCount = (int) orders.stream()
                    .filter(order -> order.status() == OrderStatus.FILLED)
                    .count();

            BigDecimal requestedQty = orders.stream()
                    .map(Order::quantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal filledQty = orders.stream()
                    .map(Order::filledQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal fillRatePct = requestedQty.compareTo(BigDecimal.ZERO) > 0
                    ? scale6(filledQty.divide(requestedQty, 6, RoundingMode.HALF_UP).multiply(new BigDecimal("100")))
                    : BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);

            List<Trade> trades = orders.stream()
                    .map(order -> tradeStoreByOrder.getOrDefault(order.orderId(), List.of()))
                    .flatMap(List::stream)
                    .toList();

            int tradeCount = trades.size();

            BigDecimal executedQuantity = trades.stream()
                    .map(Trade::tradeQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal executedNotional = trades.stream()
                    .map(Trade::notional)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalFee = trades.stream()
                    .map(Trade::fee)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalSlippage = trades.stream()
                    .map(Trade::slippage)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal averageFillPrice = executedQuantity.compareTo(BigDecimal.ZERO) > 0
                    ? scale6(executedNotional.divide(executedQuantity, 6, RoundingMode.HALF_UP))
                    : BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);

            BigDecimal averageFeeBps = executedNotional.compareTo(BigDecimal.ZERO) > 0
                    ? scale6(totalFee.multiply(BPS_DENOMINATOR).divide(executedNotional, 6, RoundingMode.HALF_UP))
                    : BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);

            BigDecimal averageSlippageBps = executedNotional.compareTo(BigDecimal.ZERO) > 0
                    ? scale6(totalSlippage.multiply(BPS_DENOMINATOR).divide(executedNotional, 6, RoundingMode.HALF_UP))
                    : BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);

            BigDecimal netCashFlow = trades.stream()
                    .map(Trade::netCashFlow)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Instant lastTradedAt = trades.stream()
                    .map(Trade::tradedAt)
                    .max(Comparator.naturalOrder())
                    .orElse(null);

            RiskLimit riskLimit = ensureRiskLimit(scopedPortfolioId);
            String qualityGrade = evaluateExecutionGrade(fillRatePct, averageSlippageBps, tradeCount, riskLimit.slippageBps());
            String qualityNote = buildExecutionQualityNote(qualityGrade, fillRatePct, averageSlippageBps);

            rows.add(new ExecutionQualityView(
                    scopedPortfolioId + "_" + scopedSymbol,
                    scopedPortfolioId,
                    scopedSymbol,
                    orderCount,
                    filledOrderCount,
                    fillRatePct,
                    tradeCount,
                    scale6(executedQuantity),
                    scale6(executedNotional),
                    averageFillPrice,
                    averageFeeBps,
                    averageSlippageBps,
                    scale6(netCashFlow),
                    qualityGrade,
                    qualityNote,
                    lastTradedAt
            ));
        }

        return rows.stream()
                .sorted(Comparator.comparing(ExecutionQualityView::portfolioId).thenComparing(ExecutionQualityView::symbol))
                .toList();
    }

    public List<OrderHealthView> searchOrderHealth(Long portfolioId, String symbol, Integer staleMinutes) {
        String normalizedSymbol = null;
        if (symbol != null && !symbol.isBlank()) {
            normalizedSymbol = normalizeSymbol(symbol);
        }

        int staleThresholdMinutes = staleMinutes == null ? DEFAULT_STALE_MINUTES : staleMinutes;
        if (staleThresholdMinutes < 0) {
            throw new IllegalArgumentException("staleMinutes must be >= 0");
        }

        Instant now = Instant.now();
        Map<String, List<Order>> groupedOrders = new HashMap<>();
        for (Order order : orderStore.values()) {
            if (portfolioId != null && !Objects.equals(order.portfolioId(), portfolioId)) {
                continue;
            }
            if (normalizedSymbol != null && !order.symbol().equalsIgnoreCase(normalizedSymbol)) {
                continue;
            }
            groupedOrders.computeIfAbsent(key(order.portfolioId(), order.symbol()), k -> new ArrayList<>()).add(order);
        }

        List<OrderHealthView> rows = new ArrayList<>();
        for (List<Order> orders : groupedOrders.values()) {
            if (orders.isEmpty()) {
                continue;
            }

            Order pivot = orders.get(0);
            Long scopedPortfolioId = pivot.portfolioId();
            String scopedSymbol = pivot.symbol().toUpperCase(Locale.ROOT);
            RiskLimit riskLimit = ensureRiskLimit(scopedPortfolioId);

            List<Order> openOrders = orders.stream()
                    .filter(order -> isOpenOrderStatus(order.status()))
                    .toList();

            int openOrderCount = openOrders.size();
            List<Order> staleOrders = openOrders.stream()
                    .filter(order -> orderAgeMinutes(order.createdAt(), now) >= staleThresholdMinutes)
                    .toList();
            int staleOrderCount = staleOrders.size();

            long maxOpenAgeMinutesLong = openOrders.stream()
                    .map(order -> orderAgeMinutes(order.createdAt(), now))
                    .max(Long::compareTo)
                    .orElse(0L);

            BigDecimal averageOpenAgeMinutes = openOrders.isEmpty()
                    ? BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP)
                    : scale6(BigDecimal.valueOf(
                    openOrders.stream()
                            .map(order -> orderAgeMinutes(order.createdAt(), now))
                            .mapToLong(Long::longValue)
                            .average()
                            .orElse(0.0)
            ));

            BigDecimal openOrderUsagePct = riskLimit.maxOpenOrdersPerSymbol() == null || riskLimit.maxOpenOrdersPerSymbol() <= 0
                    ? BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP)
                    : scale6(BigDecimal.valueOf(openOrderCount)
                    .divide(BigDecimal.valueOf(riskLimit.maxOpenOrdersPerSymbol()), 6, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")));

            Long oldestOpenOrderId = openOrders.stream()
                    .max(Comparator.comparing(order -> orderAgeMinutes(order.createdAt(), now)))
                    .map(Order::orderId)
                    .orElse(null);

            String healthStatus = evaluateOrderHealthStatus(
                    openOrderCount,
                    staleOrderCount,
                    maxOpenAgeMinutesLong,
                    staleThresholdMinutes,
                    openOrderUsagePct
            );
            String healthNote = buildOrderHealthNote(healthStatus, staleOrderCount, staleThresholdMinutes, openOrderUsagePct);

            rows.add(new OrderHealthView(
                    scopedPortfolioId + "_" + scopedSymbol,
                    scopedPortfolioId,
                    scopedSymbol,
                    openOrderCount,
                    staleOrderCount,
                    staleThresholdMinutes,
                    openOrderUsagePct,
                    averageOpenAgeMinutes,
                    maxOpenAgeMinutesLong,
                    oldestOpenOrderId,
                    healthStatus,
                    healthNote,
                    now
            ));
        }

        return rows.stream()
                .sorted(Comparator.comparing(OrderHealthView::portfolioId).thenComparing(OrderHealthView::symbol))
                .toList();
    }

    public synchronized StaleOrderRemediationResult remediateStaleOrders(
            Long portfolioId,
            String symbol,
            Integer staleMinutes,
            String reason,
            String actor
    ) {
        requirePortfolioId(portfolioId);
        final String normalizedSymbol = (symbol != null && !symbol.isBlank())
                ? normalizeSymbol(symbol)
                : null;

        int staleThresholdMinutes = staleMinutes == null ? DEFAULT_STALE_MINUTES : staleMinutes;
        if (staleThresholdMinutes < 0) {
            throw new IllegalArgumentException("staleMinutes must be >= 0");
        }

        Instant now = Instant.now();
        String normalizedReason = reason == null || reason.isBlank()
                ? "auto stale order remediation"
                : reason.trim();
        String normalizedActor = normalizeActor(actor);

        List<Order> openOrders = orderStore.values().stream()
                .filter(order -> Objects.equals(order.portfolioId(), portfolioId))
                .filter(order -> normalizedSymbol == null || order.symbol().equalsIgnoreCase(normalizedSymbol))
                .filter(order -> isOpenOrderStatus(order.status()))
                .sorted(Comparator.comparing(Order::createdAt).thenComparing(Order::orderId))
                .toList();

        List<Order> staleOrders = openOrders.stream()
                .filter(order -> orderAgeMinutes(order.createdAt(), now) >= staleThresholdMinutes)
                .toList();

        List<StaleOrderRemediationItem> items = new ArrayList<>();
        for (Order staleOrder : staleOrders) {
            long ageMinutes = orderAgeMinutes(staleOrder.createdAt(), now);
            String itemReason = normalizedReason + " (" + staleThresholdMinutes + "m+)";
            Order canceled = cancelOrder(staleOrder.orderId(), itemReason, normalizedActor);
            items.add(new StaleOrderRemediationItem(
                    canceled.orderId(),
                    canceled.portfolioId(),
                    canceled.symbol(),
                    staleOrder.status().name(),
                    canceled.status().name(),
                    ageMinutes,
                    canceled.decisionReason(),
                    canceled.decidedAt()
            ));
        }

        return new StaleOrderRemediationResult(
                portfolioId,
                normalizedSymbol,
                staleThresholdMinutes,
                openOrders.size(),
                staleOrders.size(),
                items.size(),
                items,
                Instant.now()
        );
    }

    public record OrderAuditActionCounterView(
            String action,
            Long count
    ) {
    }

    public record OrderAuditTransitionCounterView(
            String fromStatus,
            String toStatus,
            Long count
    ) {
    }

    public record OrderAuditActorCounterView(
            String actor,
            Long count
    ) {
    }

    public record OrderAuditSummaryView(
            Long portfolioId,
            Integer recentMinutes,
            Long totalCount,
            Long recentCount,
            Long distinctOrderCount,
            Instant lastActedAt,
            Instant generatedAt,
            List<OrderAuditActionCounterView> actionCounters,
            List<OrderAuditTransitionCounterView> transitionCounters,
            List<OrderAuditActorCounterView> topActors
    ) {
    }

    public record RiskAlertView(
            String alertKey,
            Long portfolioId,
            String severity,
            String code,
            String message,
            String metricName,
            BigDecimal metricValue,
            BigDecimal thresholdValue,
            Instant occurredAt
    ) {
    }

    public record RiskAlertOperationalView(
            Long ageMinutes,
            Integer slaTargetMinutes,
            Boolean slaBreached,
            Integer priorityScore
    ) {
    }

    public record RiskAlertOverviewView(
            Long portfolioId,
            Long totalCount,
            Long criticalCount,
            Long warnCount,
            Long infoCount,
            Long unacknowledgedCount,
            Long openCount,
            Long inProgressCount,
            Long resolvedCount,
            Long slaBreachedCount,
            Long oldestOpenAgeMinutes,
            Long avgAckMinutes,
            Long avgResolveMinutes,
            Instant generatedAt
    ) {
    }

    public record RiskAlertAcknowledgementView(
            String alertKey,
            Long portfolioId,
            String code,
            Boolean acknowledged,
            String note,
            String acknowledgedBy,
            Instant acknowledgedAt,
            String workflowStatus,
            String assignee,
            String resolvedBy,
            Instant resolvedAt,
            String workflowUpdatedBy,
            Instant workflowUpdatedAt
    ) {
    }

    public record ExecutionQualityView(
            String qualityKey,
            Long portfolioId,
            String symbol,
            Integer orderCount,
            Integer filledOrderCount,
            BigDecimal fillRatePct,
            Integer tradeCount,
            BigDecimal executedQuantity,
            BigDecimal executedNotional,
            BigDecimal averageFillPrice,
            BigDecimal averageFeeBps,
            BigDecimal averageSlippageBps,
            BigDecimal netCashFlow,
            String qualityGrade,
            String qualityNote,
            Instant lastTradedAt
    ) {
    }

    public record OrderHealthView(
            String healthKey,
            Long portfolioId,
            String symbol,
            Integer openOrderCount,
            Integer staleOrderCount,
            Integer staleThresholdMinutes,
            BigDecimal openOrderUsagePct,
            BigDecimal averageOpenAgeMinutes,
            Long maxOpenAgeMinutes,
            Long oldestOpenOrderId,
            String healthStatus,
            String healthNote,
            Instant updatedAt
    ) {
    }

    public record StaleOrderRemediationItem(
            Long orderId,
            Long portfolioId,
            String symbol,
            String previousStatus,
            String currentStatus,
            Long orderAgeMinutes,
            String reason,
            Instant decidedAt
    ) {
    }

    public record StaleOrderRemediationResult(
            Long portfolioId,
            String symbol,
            Integer staleThresholdMinutes,
            Integer evaluatedOpenOrderCount,
            Integer staleOrderCount,
            Integer canceledCount,
            List<StaleOrderRemediationItem> items,
            Instant executedAt
    ) {
    }

    public record PortfolioTopExposureView(
            String symbol,
            BigDecimal quantity,
            BigDecimal marketValue,
            BigDecimal grossExposureWeightPct,
            BigDecimal totalPnl
    ) {
    }

    public record PortfolioSummaryInsightView(
            Long portfolioId,
            Integer healthScore,
            String healthStatus,
            BigDecimal pnlMarginPct,
            BigDecimal turnoverUsagePct,
            BigDecimal openOrderRatioPct,
            BigDecimal orderPressurePct,
            Integer criticalAlertCount,
            Integer warnAlertCount,
            Boolean tradingEnabled,
            String topConcentrationSymbol,
            BigDecimal topConcentrationWeightPct,
            BigDecimal grossExposure,
            BigDecimal marketValue,
            BigDecimal realizedPnl,
            BigDecimal unrealizedPnl,
            BigDecimal totalPnl,
            BigDecimal dailyTurnover,
            Integer openOrderCount,
            Integer filledOrderCount,
            Integer tradeCount,
            Integer positionCount,
            Instant generatedAt,
            List<PortfolioTopExposureView> topExposures
    ) {
    }

    public record ProfitPlaybookActionView(
            String actionKey,
            String severity,
            Boolean blocker,
            String title,
            String description,
            String expectedImpact,
            String ownerRole,
            String horizon,
            String path
    ) {
    }

    public record ProfitPlaybookActionSnapshotView(
            Long portfolioId,
            Integer healthScore,
            String healthStatus,
            BigDecimal totalPnl,
            BigDecimal turnoverUsagePct,
            Integer openOrderCount,
            Integer criticalAlertCount,
            Integer warnAlertCount,
            Boolean tradingEnabled,
            Instant capturedAt
    ) {
    }

    public record ProfitPlaybookActionFeedbackView(
            Long feedbackId,
            Long portfolioId,
            String actionKey,
            String actionTitle,
            String sourceTaskKey,
            String outcomeStatus,
            String reason,
            String executedBy,
            Instant executedAt,
            ProfitPlaybookActionSnapshotView beforeSnapshot,
            ProfitPlaybookActionSnapshotView afterSnapshot,
            BigDecimal deltaTotalPnl,
            BigDecimal deltaTurnoverUsagePct,
            Integer deltaOpenOrderCount,
            Integer deltaCriticalAlertCount,
            Integer deltaWarnAlertCount,
            String outcomeEvaluation
    ) {
    }

    public record PortfolioProfitPlaybookView(
            Long portfolioId,
            String objective,
            String objectiveDetail,
            String strategyFocus,
            String marketRegime,
            String executionGuideline,
            Boolean tradable,
            Integer priorityScore,
            Integer blockerCount,
            List<ProfitPlaybookActionView> actions,
            Instant generatedAt
    ) {
    }

    public record TradingResumeResult(
            Long portfolioId,
            Boolean tradingEnabled,
            Boolean resumed,
            Integer blockedCriticalCount,
            List<String> blockedCodes,
            List<String> blockedMessages,
            Instant executedAt,
            String executedBy
    ) {
    }

    public record TradingControlHistoryView(
            Long historyId,
            Long portfolioId,
            Boolean previousTradingEnabled,
            Boolean tradingEnabled,
            String action,
            String reason,
            Instant updatedAt,
            String updatedBy
    ) {
    }

    public record OrderInsightView(
            Order order,
            BigDecimal remainingQuantity,
            BigDecimal fillRatePct,
            BigDecimal requestedNotional,
            BigDecimal executedNotional,
            BigDecimal averageExecutionPrice,
            BigDecimal totalFee,
            BigDecimal totalSlippage,
            BigDecimal netCashFlow,
            Long staleMinutes,
            Boolean cancelable,
            Boolean rejectable,
            List<Trade> trades,
            List<OrderAuditLog> audits,
            List<RiskAlertView> riskAlerts
    ) {
    }

    public RiskLimit getRiskLimit(Long portfolioId) {
        requirePortfolioId(portfolioId);
        return ensureRiskLimit(portfolioId);
    }

    public List<RiskLimit> searchRiskLimits(Long portfolioId) {
        if (portfolioId != null) {
            return List.of(getRiskLimit(portfolioId));
        }

        Set<Long> portfolioIds = new TreeSet<>();
        portfolioIds.addAll(riskLimitStore.keySet());
        orderStore.values().forEach(order -> portfolioIds.add(order.portfolioId()));
        positionStore.values().forEach(position -> portfolioIds.add(position.portfolioId()));

        return portfolioIds.stream()
                .map(this::ensureRiskLimit)
                .sorted(Comparator.comparing(RiskLimit::portfolioId))
                .toList();
    }

    public List<PortfolioSummary> searchPortfolioSummaries(Long portfolioId) {
        Set<Long> portfolioIds = new TreeSet<>();

        if (portfolioId != null) {
            portfolioIds.add(portfolioId);
        } else {
            portfolioIds.addAll(riskLimitStore.keySet());
            orderStore.values().forEach(order -> portfolioIds.add(order.portfolioId()));
            positionStore.values().forEach(position -> portfolioIds.add(position.portfolioId()));
        }

        return portfolioIds.stream()
                .map(this::toPortfolioSummary)
                .sorted(Comparator.comparing(PortfolioSummary::portfolioId))
                .toList();
    }

    public List<PortfolioSummaryInsightView> searchPortfolioSummaryInsights(Long portfolioId) {
        Instant generatedAt = Instant.now();
        return searchPortfolioSummaries(portfolioId).stream()
                .map(summary -> toPortfolioSummaryInsight(summary, generatedAt))
                .sorted(Comparator.comparing(PortfolioSummaryInsightView::portfolioId))
                .toList();
    }

    public List<PortfolioProfitPlaybookView> searchPortfolioProfitPlaybooks(Long portfolioId) {
        Instant generatedAt = Instant.now();
        return searchPortfolioSummaryInsights(portfolioId).stream()
                .map(insight -> toPortfolioProfitPlaybook(insight, generatedAt))
                .sorted(Comparator.comparing(PortfolioProfitPlaybookView::portfolioId))
                .toList();
    }

    public ProfitPlaybookActionSnapshotView captureProfitPlaybookSnapshot(Long portfolioId) {
        requirePortfolioId(portfolioId);
        PortfolioSummaryInsightView insight = searchPortfolioSummaryInsights(portfolioId).stream()
                .findFirst()
                .orElseGet(() -> toPortfolioSummaryInsight(toPortfolioSummary(portfolioId), Instant.now()));
        return toPlaybookSnapshot(insight, Instant.now());
    }

    public ProfitPlaybookActionFeedbackView appendProfitPlaybookActionFeedback(
            Long portfolioId,
            String actionKey,
            String actionTitle,
            String sourceTaskKey,
            String outcomeStatus,
            String reason,
            String executedBy,
            ProfitPlaybookActionSnapshotView beforeSnapshot,
            ProfitPlaybookActionSnapshotView afterSnapshot
    ) {
        requirePortfolioId(portfolioId);
        if (beforeSnapshot == null || afterSnapshot == null) {
            throw new IllegalArgumentException("beforeSnapshot and afterSnapshot are required");
        }

        ProfitPlaybookActionFeedbackView feedback = new ProfitPlaybookActionFeedbackView(
                playbookFeedbackSeq.incrementAndGet(),
                portfolioId,
                normalizeActionKey(actionKey),
                normalizeText(actionTitle, "액션"),
                normalizeActionKey(sourceTaskKey),
                normalizeOutcomeStatus(outcomeStatus),
                normalizeText(reason, ""),
                normalizeActor(executedBy),
                Instant.now(),
                beforeSnapshot,
                afterSnapshot,
                scale6(nz(afterSnapshot.totalPnl()).subtract(nz(beforeSnapshot.totalPnl()))),
                scale6(nz(afterSnapshot.turnoverUsagePct()).subtract(nz(beforeSnapshot.turnoverUsagePct()))),
                safeInt(afterSnapshot.openOrderCount()) - safeInt(beforeSnapshot.openOrderCount()),
                safeInt(afterSnapshot.criticalAlertCount()) - safeInt(beforeSnapshot.criticalAlertCount()),
                safeInt(afterSnapshot.warnAlertCount()) - safeInt(beforeSnapshot.warnAlertCount()),
                evaluatePlaybookActionOutcome(beforeSnapshot, afterSnapshot, outcomeStatus)
        );

        playbookFeedbackStoreByPortfolio
                .computeIfAbsent(portfolioId, ignored -> new CopyOnWriteArrayList<>())
                .add(feedback);
        return feedback;
    }

    public List<ProfitPlaybookActionFeedbackView> searchProfitPlaybookActionFeedbacks(Long portfolioId, Integer limit) {
        int normalizedLimit = normalizeFeedbackLimit(limit);
        if (portfolioId != null) {
            requirePortfolioId(portfolioId);
            return playbookFeedbackStoreByPortfolio.getOrDefault(portfolioId, List.of()).stream()
                    .sorted(Comparator.comparing(ProfitPlaybookActionFeedbackView::executedAt).reversed()
                            .thenComparing(ProfitPlaybookActionFeedbackView::feedbackId, Comparator.reverseOrder()))
                    .limit(normalizedLimit)
                    .toList();
        }

        return playbookFeedbackStoreByPortfolio.values().stream()
                .flatMap(List::stream)
                .sorted(Comparator.comparing(ProfitPlaybookActionFeedbackView::executedAt).reversed()
                        .thenComparing(ProfitPlaybookActionFeedbackView::feedbackId, Comparator.reverseOrder()))
                .limit(normalizedLimit)
                .toList();
    }

    private ProfitPlaybookActionSnapshotView toPlaybookSnapshot(PortfolioSummaryInsightView insight, Instant capturedAt) {
        return new ProfitPlaybookActionSnapshotView(
                insight.portfolioId(),
                insight.healthScore(),
                insight.healthStatus(),
                scale6(nz(insight.totalPnl())),
                scale6(nz(insight.turnoverUsagePct())),
                insight.openOrderCount(),
                insight.criticalAlertCount(),
                insight.warnAlertCount(),
                insight.tradingEnabled(),
                capturedAt
        );
    }

    private int normalizeFeedbackLimit(Integer limit) {
        if (limit == null) {
            return 20;
        }
        return Math.max(1, Math.min(limit, 100));
    }

    private String normalizeActionKey(String actionKey) {
        if (actionKey == null || actionKey.isBlank()) {
            return "unknown";
        }
        return actionKey.trim();
    }

    private String normalizeOutcomeStatus(String outcomeStatus) {
        if (outcomeStatus == null || outcomeStatus.isBlank()) {
            return "SUCCESS";
        }
        return outcomeStatus.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeText(String text, String defaultValue) {
        if (text == null || text.isBlank()) {
            return defaultValue;
        }
        return text.trim();
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String evaluatePlaybookActionOutcome(
            ProfitPlaybookActionSnapshotView beforeSnapshot,
            ProfitPlaybookActionSnapshotView afterSnapshot,
            String outcomeStatus
    ) {
        if (!"SUCCESS".equalsIgnoreCase(outcomeStatus)) {
            return "FAILED";
        }

        int score = 0;
        if (safeInt(afterSnapshot.criticalAlertCount()) < safeInt(beforeSnapshot.criticalAlertCount())) {
            score += 3;
        }
        if (safeInt(afterSnapshot.warnAlertCount()) < safeInt(beforeSnapshot.warnAlertCount())) {
            score += 2;
        }
        if (safeInt(afterSnapshot.openOrderCount()) < safeInt(beforeSnapshot.openOrderCount())) {
            score += 2;
        }
        if (nz(afterSnapshot.totalPnl()).compareTo(nz(beforeSnapshot.totalPnl())) > 0) {
            score += 1;
        }
        if (nz(afterSnapshot.turnoverUsagePct()).compareTo(nz(beforeSnapshot.turnoverUsagePct())) > 0) {
            score -= 1;
        }

        if (score >= 3) {
            return "POSITIVE";
        }
        if (score <= -1) {
            return "NEGATIVE";
        }
        return "NEUTRAL";
    }

    private PortfolioProfitPlaybookView toPortfolioProfitPlaybook(
            PortfolioSummaryInsightView insight,
            Instant generatedAt
    ) {
        List<ProfitPlaybookActionView> actions = new ArrayList<>();
        int blockerCount = 0;

        if (Boolean.FALSE.equals(insight.tradingEnabled())) {
            blockerCount += 1;
            actions.add(new ProfitPlaybookActionView(
                    "resumeTradingGuarded",
                    "CRITICAL",
                    true,
                    "거래 재개 가드 점검",
                    "현재 거래가 중지 상태입니다. 리스크 경보 해소 후 재개 여부를 승인하세요.",
                    "비정상 구간에서의 손실 확장 차단",
                    "RISK",
                    "즉시",
                    "/#/riskLimits"
            ));
        }

        if (insight.criticalAlertCount() != null && insight.criticalAlertCount() > 0) {
            blockerCount += 1;
            actions.add(new ProfitPlaybookActionView(
                    "resolveCriticalAlerts",
                    "CRITICAL",
                    true,
                    "치명 경보 우선 해소",
                    "CRITICAL 경보가 존재합니다. 해결 전 신규 익스포저 확대를 중단하세요.",
                    "급격한 손실/운영사고 예방",
                    "RISK",
                    "즉시",
                    "/#/riskAlerts"
            ));
        }

        if (nz(insight.turnoverUsagePct()).compareTo(new BigDecimal("85")) >= 0) {
            actions.add(new ProfitPlaybookActionView(
                    "rebalanceTurnoverBudget",
                    "WARN",
                    false,
                    "회전 한도 예산 재배분",
                    "회전 한도 사용률이 높습니다. 주문 우선순위를 조정하고 비핵심 주문을 축소하세요.",
                    "수수료/슬리피지 누적 손실 감소",
                    "TRADER",
                    "당일",
                    "/#/riskLimits"
            ));
        }

        if (nz(insight.openOrderRatioPct()).compareTo(new BigDecimal("40")) >= 0) {
            actions.add(new ProfitPlaybookActionView(
                    "reduceOrderBacklog",
                    "WARN",
                    false,
                    "오픈 주문 적체 해소",
                    "미체결 주문 비율이 높습니다. 지연 주문 정리 및 재호가 정책을 적용하세요.",
                    "체결 지연으로 인한 기회손실 축소",
                    "TRADER",
                    "당일",
                    "/#/orderHealth"
            ));
        }

        if (nz(insight.topConcentrationWeightPct()).compareTo(new BigDecimal("35")) >= 0) {
            actions.add(new ProfitPlaybookActionView(
                    "rebalanceConcentration",
                    "WARN",
                    false,
                    "집중도 완화 리밸런싱",
                    "상위 종목 집중도가 높습니다. 분산 비중을 늘려 단일 종목 리스크를 낮추세요.",
                    "종목 특이 이벤트 손실 완화",
                    "PM",
                    "1~3일",
                    "/#/positions"
            ));
        }

        if (nz(insight.pnlMarginPct()).compareTo(BigDecimal.ZERO) < 0) {
            actions.add(new ProfitPlaybookActionView(
                    "improveExecutionQuality",
                    "WARN",
                    false,
                    "음수 손익 구간 원인 분석",
                    "현재 손익 마진이 음수입니다. 체결 품질/슬리피지/전략 신호 정확도를 점검하세요.",
                    "손실 지속 구간 단축",
                    "TRADER",
                    "당일",
                    "/#/executionQualities"
            ));
        }

        if (actions.isEmpty()) {
            actions.add(new ProfitPlaybookActionView(
                    "expandHighConvictionIdeas",
                    "INFO",
                    false,
                    "고신뢰 아이디어 확장",
                    "핵심 리스크 지표가 안정적입니다. 체결 품질이 우수한 종목 중심으로 실행을 확대하세요.",
                    "리스크 통제 하 수익 기회 확대",
                    "PM",
                    "당일",
                    "/#/orders"
            ));
        }

        int healthScore = insight.healthScore() == null ? 50 : Math.max(0, Math.min(100, insight.healthScore()));
        int warnCount = insight.warnAlertCount() == null ? 0 : Math.max(0, insight.warnAlertCount());
        int priorityScore = Math.min(100, Math.max(0, (100 - healthScore) + (blockerCount * 20) + (warnCount * 5)));

        String strategyFocus = insight.topConcentrationSymbol() == null
                ? "신호 기반 분산 실행"
                : "핵심 종목(" + insight.topConcentrationSymbol() + ") 중심 실행 + 분산 유지";
        String marketRegime = resolveProfitMarketRegime(insight, blockerCount);

        return new PortfolioProfitPlaybookView(
                insight.portfolioId(),
                "리스크 조정 수익률 극대화",
                "손실 확장 구간을 먼저 차단하고, 체결 품질이 검증된 주문만 우선 집행해 누적 수익을 만든다.",
                strategyFocus,
                marketRegime,
                "경보/한도/체결품질 게이트 통과 후에만 익스포저를 확대한다.",
                Boolean.TRUE.equals(insight.tradingEnabled()),
                priorityScore,
                blockerCount,
                actions,
                generatedAt
        );
    }

    private String resolveProfitMarketRegime(PortfolioSummaryInsightView insight, int blockerCount) {
        if (blockerCount > 0) {
            return "RISK_OFF";
        }
        if (nz(insight.pnlMarginPct()).compareTo(BigDecimal.ZERO) < 0) {
            return "DEFENSIVE";
        }
        if (nz(insight.turnoverUsagePct()).compareTo(new BigDecimal("80")) >= 0) {
            return "CAUTION";
        }
        return "RISK_ON";
    }

    private PortfolioSummaryInsightView toPortfolioSummaryInsight(PortfolioSummary summary, Instant generatedAt) {
        Long portfolioId = summary.portfolioId();
        RiskLimit limit = ensureRiskLimit(portfolioId);
        List<Position> positions = getPositions(portfolioId);
        List<RiskAlertView> alerts = searchRiskAlerts(portfolioId);

        int criticalAlertCount = (int) alerts.stream()
                .filter(alert -> "CRITICAL".equalsIgnoreCase(alert.severity()))
                .count();
        int warnAlertCount = (int) alerts.stream()
                .filter(alert -> "WARN".equalsIgnoreCase(alert.severity()))
                .count();

        BigDecimal marketAbs = nz(summary.marketValue()).abs();
        BigDecimal pnlMarginPct = marketAbs.compareTo(BigDecimal.ZERO) > 0
                ? scale6(nz(summary.totalPnl()).divide(marketAbs, 6, RoundingMode.HALF_UP).multiply(new BigDecimal("100")))
                : BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);

        int openOrders = summary.openOrderCount();
        int filledOrders = summary.filledOrderCount();
        int totalOrders = openOrders + filledOrders;
        BigDecimal openOrderRatioPct = totalOrders > 0
                ? scale6(BigDecimal.valueOf(openOrders)
                .divide(BigDecimal.valueOf(totalOrders), 6, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")))
                : BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);

        BigDecimal orderPressurePct = limit.maxOpenOrdersPerSymbol() != null && limit.maxOpenOrdersPerSymbol() > 0
                ? scale6(BigDecimal.valueOf(openOrders)
                .divide(BigDecimal.valueOf(limit.maxOpenOrdersPerSymbol()), 6, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")))
                : BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);

        List<PortfolioTopExposureView> topExposures = positions.stream()
                .sorted(Comparator.comparing((Position p) -> p.marketValue().abs()).reversed()
                        .thenComparing(Position::symbol))
                .limit(5)
                .map(position -> {
                    BigDecimal grossExposure = nz(summary.grossExposure());
                    BigDecimal weightPct = grossExposure.compareTo(BigDecimal.ZERO) > 0
                            ? scale6(position.marketValue().abs()
                            .divide(grossExposure, 6, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100")))
                            : BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);
                    BigDecimal totalPnl = nz(position.realizedPnl()).add(nz(position.unrealizedPnl()));
                    return new PortfolioTopExposureView(
                            position.symbol(),
                            scale6(nz(position.quantity())),
                            scale6(nz(position.marketValue())),
                            weightPct,
                            scale6(totalPnl)
                    );
                })
                .toList();

        String topConcentrationSymbol = topExposures.isEmpty() ? null : topExposures.get(0).symbol();
        BigDecimal topConcentrationWeightPct = topExposures.isEmpty()
                ? BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP)
                : topExposures.get(0).grossExposureWeightPct();

        int healthScore = calculatePortfolioHealthScore(
                nz(summary.turnoverUsagePct()),
                openOrderRatioPct,
                pnlMarginPct,
                criticalAlertCount,
                warnAlertCount,
                Boolean.TRUE.equals(limit.tradingEnabled())
        );
        String healthStatus = evaluatePortfolioHealthStatus(
                healthScore,
                criticalAlertCount,
                warnAlertCount,
                Boolean.TRUE.equals(limit.tradingEnabled())
        );

        return new PortfolioSummaryInsightView(
                portfolioId,
                healthScore,
                healthStatus,
                pnlMarginPct,
                nz(summary.turnoverUsagePct()),
                openOrderRatioPct,
                orderPressurePct,
                criticalAlertCount,
                warnAlertCount,
                limit.tradingEnabled(),
                topConcentrationSymbol,
                topConcentrationWeightPct,
                scale6(nz(summary.grossExposure())),
                scale6(nz(summary.marketValue())),
                scale6(nz(summary.realizedPnl())),
                scale6(nz(summary.unrealizedPnl())),
                scale6(nz(summary.totalPnl())),
                scale6(nz(summary.dailyTurnover())),
                openOrders,
                filledOrders,
                summary.tradeCount(),
                summary.positionCount(),
                generatedAt,
                topExposures
        );
    }

    private PortfolioSummary toPortfolioSummary(Long portfolioId) {
        RiskLimit limit = ensureRiskLimit(portfolioId);
        List<Position> positions = getPositions(portfolioId);

        BigDecimal marketValue = positions.stream()
                .map(Position::marketValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal grossExposure = positions.stream()
                .map(p -> p.marketValue().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal realized = positions.stream()
                .map(Position::realizedPnl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal unrealized = positions.stream()
                .map(Position::unrealizedPnl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPnl = realized.add(unrealized);
        BigDecimal dailyTurnover = todayTurnoverNotional(portfolioId);

        int openOrders = (int) orderStore.values().stream()
                .filter(order -> Objects.equals(order.portfolioId(), portfolioId))
                .filter(order -> isOpenOrderStatus(order.status()))
                .count();

        int filledOrders = (int) orderStore.values().stream()
                .filter(order -> Objects.equals(order.portfolioId(), portfolioId))
                .filter(order -> order.status() == OrderStatus.FILLED)
                .count();

        int tradeCount = (int) tradeStoreById.values().stream()
                .filter(trade -> belongsToPortfolio(trade, portfolioId))
                .count();

        BigDecimal turnoverUsagePct = limit.maxDailyTurnover().compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : dailyTurnover.divide(limit.maxDailyTurnover(), 6, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        return new PortfolioSummary(
                portfolioId,
                scale6(grossExposure),
                scale6(marketValue),
                scale6(realized),
                scale6(unrealized),
                scale6(totalPnl),
                scale6(dailyTurnover),
                scale6(turnoverUsagePct),
                openOrders,
                filledOrders,
                tradeCount,
                positions.size()
        );
    }

    private void recalculatePosition(Long portfolioId, String symbol) {
        List<Trade> scopedTrades = tradeStoreByOrder.entrySet().stream()
                .filter(entry -> {
                    Order order = orderStore.get(entry.getKey());
                    return order != null
                            && Objects.equals(order.portfolioId(), portfolioId)
                            && order.symbol().equalsIgnoreCase(symbol);
                })
                .flatMap(entry -> entry.getValue().stream())
                .sorted(Comparator.comparing(Trade::tradedAt).thenComparing(Trade::tradeId))
                .toList();

        if (scopedTrades.isEmpty()) {
            positionStore.remove(key(portfolioId, symbol));
            return;
        }

        BigDecimal qty = BigDecimal.ZERO;
        BigDecimal avg = BigDecimal.ZERO;
        BigDecimal realized = BigDecimal.ZERO;
        BigDecimal lastPrice = BigDecimal.ZERO;

        for (Trade trade : scopedTrades) {
            BigDecimal tradeQty = trade.tradeQuantity();
            BigDecimal tradePrice = trade.tradePrice();
            lastPrice = tradePrice;

            if (trade.side() == OrderSide.BUY) {
                BigDecimal effectiveBuyCost = trade.notional().add(trade.fee()).add(trade.slippage());
                BigDecimal newQty = qty.add(tradeQty);
                BigDecimal totalCost = avg.multiply(qty).add(effectiveBuyCost);
                avg = totalCost.divide(newQty, 6, RoundingMode.HALF_UP);
                qty = newQty;
                continue;
            }

            if (tradeQty.compareTo(qty) > 0) {
                throw new IllegalStateException(
                        "sell trade exceeds position while recalculating. symbol=" + symbol + ", qty=" + qty
                                + ", sell=" + tradeQty);
            }

            BigDecimal effectiveSellProceeds = trade.notional().subtract(trade.fee()).subtract(trade.slippage());
            BigDecimal costBasis = avg.multiply(tradeQty);
            realized = realized.add(effectiveSellProceeds.subtract(costBasis));
            qty = qty.subtract(tradeQty);

            if (qty.compareTo(BigDecimal.ZERO) == 0) {
                avg = BigDecimal.ZERO;
            }
        }

        BigDecimal marketValue = qty.multiply(lastPrice).setScale(6, RoundingMode.HALF_UP);
        BigDecimal unrealized = qty.multiply(lastPrice.subtract(avg)).setScale(6, RoundingMode.HALF_UP);

        positionStore.put(
                key(portfolioId, symbol),
                new Position(
                        portfolioId,
                        symbol.toUpperCase(),
                        qty.setScale(6, RoundingMode.HALF_UP),
                        avg.setScale(6, RoundingMode.HALF_UP),
                        lastPrice.setScale(6, RoundingMode.HALF_UP),
                        marketValue,
                        unrealized,
                        realized.setScale(6, RoundingMode.HALF_UP)
                )
        );
    }

    private void enforceOrderRiskLimit(
            RiskLimit riskLimit,
            Long portfolioId,
            String symbol,
            OrderSide side,
            BigDecimal quantity,
            BigDecimal referencePrice,
            BigDecimal estimatedNotional
    ) {
        if (estimatedNotional.compareTo(riskLimit.maxOrderNotional()) > 0) {
            throw new IllegalArgumentException(
                    "order notional exceeds limit. limit=" + riskLimit.maxOrderNotional()
                            + ", requested=" + estimatedNotional);
        }

        long openOrders = orderStore.values().stream()
                .filter(order -> Objects.equals(order.portfolioId(), portfolioId))
                .filter(order -> order.symbol().equalsIgnoreCase(symbol))
                .filter(order -> isOpenOrderStatus(order.status()))
                .count();

        if (openOrders >= riskLimit.maxOpenOrdersPerSymbol()) {
            throw new IllegalArgumentException(
                    "open orders per symbol limit exceeded. symbol=" + symbol + ", limit="
                            + riskLimit.maxOpenOrdersPerSymbol());
        }

        if (side == OrderSide.BUY) {
            BigDecimal projectedQty = currentPositionQty(portfolioId, symbol).add(quantity);
            BigDecimal projectedNotional = projectedQty.multiply(referencePrice);
            if (projectedNotional.compareTo(riskLimit.maxPositionNotionalPerSymbol()) > 0) {
                throw new IllegalArgumentException(
                        "projected position notional exceeds per-symbol limit. symbol=" + symbol
                                + ", limit=" + riskLimit.maxPositionNotionalPerSymbol()
                                + ", projected=" + scale6(projectedNotional));
            }
        }
    }

    private boolean belongsToPortfolio(Trade trade, Long portfolioId) {
        Order order = orderStore.get(trade.orderId());
        return order != null && Objects.equals(order.portfolioId(), portfolioId);
    }

    private BigDecimal resolveReferencePrice(
            Long portfolioId,
            String symbol,
            OrderType orderType,
            BigDecimal limitPrice
    ) {
        if (orderType == OrderType.LIMIT && limitPrice != null) {
            return limitPrice;
        }

        Position position = positionStore.get(key(portfolioId, symbol));
        if (position != null && position.lastPrice().compareTo(BigDecimal.ZERO) > 0) {
            return position.lastPrice();
        }

        return tradeStoreById.values().stream()
                .filter(trade -> trade.symbol().equalsIgnoreCase(symbol))
                .max(Comparator.comparing(Trade::tradedAt).thenComparing(Trade::tradeId))
                .map(Trade::tradePrice)
                .orElse(DEFAULT_PRICE);
    }

    private BigDecimal todayTurnoverNotional(Long portfolioId) {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        return tradeStoreById.values().stream()
                .filter(trade -> belongsToPortfolio(trade, portfolioId))
                .filter(trade -> LocalDate.ofInstant(trade.tradedAt(), ZoneId.systemDefault()).equals(today))
                .map(Trade::notional)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void ensureTradeAllowed(Order order) {
        if (order.status() == OrderStatus.FILLED
                || order.status() == OrderStatus.CANCELED
                || order.status() == OrderStatus.REJECTED) {
            throw new IllegalArgumentException(
                    "trade is not allowed for order status: " + order.status());
        }
    }

    private boolean isOpenOrderStatus(OrderStatus status) {
        return status == OrderStatus.NEW || status == OrderStatus.SENT || status == OrderStatus.PARTIAL;
    }

    private BigDecimal availableQuantityForSell(Long portfolioId, String symbol) {
        BigDecimal currentPosition = currentPositionQty(portfolioId, symbol);
        BigDecimal pendingSell = pendingSellQuantity(portfolioId, symbol);
        BigDecimal available = currentPosition.subtract(pendingSell);
        if (available.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return available;
    }

    private BigDecimal pendingSellQuantity(Long portfolioId, String symbol) {
        return orderStore.values().stream()
                .filter(order -> Objects.equals(order.portfolioId(), portfolioId))
                .filter(order -> order.symbol().equalsIgnoreCase(symbol))
                .filter(order -> order.side() == OrderSide.SELL)
                .filter(order -> isOpenOrderStatus(order.status()))
                .map(order -> order.quantity().subtract(order.filledQuantity()))
                .filter(remaining -> remaining.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal currentPositionQty(Long portfolioId, String symbol) {
        Position position = positionStore.get(key(portfolioId, symbol));
        if (position == null) {
            return BigDecimal.ZERO;
        }
        return position.quantity();
    }

    private Order requireOrder(Long orderId) {
        Order order = orderStore.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("order not found: " + orderId);
        }
        return order;
    }

    private void appendOrderAudit(
            Order order,
            OrderStatus fromStatus,
            OrderStatus toStatus,
            String action,
            String reason,
            String actor
    ) {
        Long auditId = orderAuditSeq.incrementAndGet();
        OrderAuditLog auditLog = new OrderAuditLog(
                auditId,
                order.orderId(),
                order.portfolioId(),
                order.symbol(),
                action,
                fromStatus,
                toStatus,
                reason,
                normalizeActor(actor),
                Instant.now()
        );
        orderAuditStoreByOrder.computeIfAbsent(order.orderId(), key -> new ArrayList<>()).add(auditLog);
    }

    private void appendTradingControlHistory(
            RiskLimit previous,
            RiskLimit updated,
            String reason,
            Instant changedAt,
            String actor
    ) {
        String action = Boolean.TRUE.equals(updated.tradingEnabled()) ? "ENABLE" : "DISABLE";
        TradingControlHistoryView view = new TradingControlHistoryView(
                tradingControlAuditSeq.incrementAndGet(),
                updated.portfolioId(),
                previous.tradingEnabled(),
                updated.tradingEnabled(),
                action,
                reason,
                changedAt,
                actor
        );
        tradingControlHistoryStoreByPortfolio
                .computeIfAbsent(updated.portfolioId(), key -> new CopyOnWriteArrayList<>())
                .add(view);
    }

    private RiskLimit ensureRiskLimit(Long portfolioId) {
        return riskLimitStore.computeIfAbsent(portfolioId, this::defaultRiskLimit);
    }

    private RiskLimit defaultRiskLimit(Long portfolioId) {
        return new RiskLimit(
                portfolioId,
                new BigDecimal("1000000"),
                new BigDecimal("3000000"),
                new BigDecimal("5000000"),
                20,
                new BigDecimal("2.5"),
                new BigDecimal("1.5"),
                true,
                null,
                Instant.now(),
                SYSTEM_ACTOR
        );
    }

    private void requirePortfolioId(Long portfolioId) {
        if (portfolioId == null || portfolioId <= 0) {
            throw new IllegalArgumentException("portfolioId must be positive");
        }
    }

    private String normalizeSymbol(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("symbol is required");
        }
        String normalized = symbol.trim().toUpperCase();
        if (!US_SYMBOL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("invalid US symbol format: " + symbol);
        }
        return normalized;
    }

    private BigDecimal normalizeQuantity(BigDecimal value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
        if (value.scale() > 4) {
            throw new IllegalArgumentException(fieldName + " scale must be <= 4");
        }
        return value.stripTrailingZeros();
    }

    private BigDecimal normalizePrice(BigDecimal tradePrice) {
        if (tradePrice == null) {
            throw new IllegalArgumentException("tradePrice is required");
        }
        if (tradePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("tradePrice must be positive");
        }
        if (tradePrice.compareTo(MAX_TRADE_PRICE) > 0) {
            throw new IllegalArgumentException("tradePrice exceeds max allowed: " + MAX_TRADE_PRICE);
        }
        if (tradePrice.scale() > 6) {
            throw new IllegalArgumentException("tradePrice scale must be <= 6");
        }
        return tradePrice.stripTrailingZeros();
    }

    private BigDecimal normalizeLimitPrice(OrderType orderType, BigDecimal limitPrice) {
        if (orderType == OrderType.MARKET) {
            if (limitPrice == null) {
                return null;
            }
            if (limitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("limitPrice must be positive");
            }
            return limitPrice.stripTrailingZeros();
        }

        if (limitPrice == null) {
            throw new IllegalArgumentException("limitPrice is required for LIMIT order");
        }
        if (limitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("limitPrice must be positive");
        }
        return limitPrice.stripTrailingZeros();
    }

    private BigDecimal requirePositive(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
        return scale6(value);
    }

    private BigDecimal requireNonNegative(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " must be non-negative");
        }
        return scale6(value);
    }

    private Integer requirePositiveInt(Integer value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
        return value;
    }

    private String normalizeActor(String actor) {
        if (actor == null || actor.isBlank()) {
            return SYSTEM_ACTOR;
        }
        return actor.trim().toLowerCase(Locale.ROOT);
    }

    private int severityRank(String severity) {
        if (severity == null) {
            return 3;
        }
        return switch (severity) {
            case "CRITICAL" -> 0;
            case "WARN" -> 1;
            case "INFO" -> 2;
            default -> 3;
        };
    }

    private String evaluateExecutionGrade(
            BigDecimal fillRatePct,
            BigDecimal averageSlippageBps,
            int tradeCount,
            BigDecimal limitSlippageBps
    ) {
        if (tradeCount <= 0) {
            return "D";
        }

        BigDecimal slippageLimit = limitSlippageBps == null
                ? new BigDecimal("2.0")
                : limitSlippageBps;
        BigDecimal aGradeSlippageCap = slippageLimit.multiply(new BigDecimal("1.25"));
        BigDecimal bGradeSlippageCap = slippageLimit.multiply(new BigDecimal("2.00"));

        if (fillRatePct.compareTo(new BigDecimal("95")) >= 0
                && averageSlippageBps.compareTo(aGradeSlippageCap) <= 0) {
            return "A";
        }
        if (fillRatePct.compareTo(new BigDecimal("85")) >= 0
                && averageSlippageBps.compareTo(bGradeSlippageCap) <= 0) {
            return "B";
        }
        if (fillRatePct.compareTo(new BigDecimal("70")) >= 0) {
            return "C";
        }
        return "D";
    }

    private String buildExecutionQualityNote(
            String qualityGrade,
            BigDecimal fillRatePct,
            BigDecimal averageSlippageBps
    ) {
        return switch (qualityGrade) {
            case "A" -> "체결률/슬리피지가 우수합니다.";
            case "B" -> "양호한 수준이나 개선 여지가 있습니다.";
            case "C" -> "체결률 또는 슬리피지 관리 강화가 필요합니다.";
            case "D" -> {
                if (fillRatePct.compareTo(new BigDecimal("50")) < 0) {
                    yield "미체결 비중이 높아 주문 전략 점검이 필요합니다.";
                }
                if (averageSlippageBps.compareTo(new BigDecimal("8")) >= 0) {
                    yield "슬리피지가 과도하여 집행 알고리즘 조정이 필요합니다.";
                }
                yield "집행 품질이 낮아 긴급 개선이 필요합니다.";
            }
            default -> "집행 품질을 점검하세요.";
        };
    }

    private String evaluateOrderHealthStatus(
            int openOrderCount,
            int staleOrderCount,
            long maxOpenAgeMinutes,
            int staleThresholdMinutes,
            BigDecimal openOrderUsagePct
    ) {
        if (openOrderCount == 0) {
            return "HEALTHY";
        }

        long criticalAgeThreshold = Math.max(1L, (long) staleThresholdMinutes * 3L);
        if (staleOrderCount >= 3
                || maxOpenAgeMinutes >= criticalAgeThreshold
                || openOrderUsagePct.compareTo(new BigDecimal("100")) >= 0) {
            return "CRITICAL";
        }
        if (staleOrderCount >= 1 || openOrderUsagePct.compareTo(new BigDecimal("80")) >= 0) {
            return "WARN";
        }
        return "HEALTHY";
    }

    private String buildOrderHealthNote(
            String healthStatus,
            int staleOrderCount,
            int staleThresholdMinutes,
            BigDecimal openOrderUsagePct
    ) {
        if ("CRITICAL".equalsIgnoreCase(healthStatus)) {
            if (staleOrderCount > 0) {
                return "장시간 미체결 주문이 누적되어 즉시 점검이 필요합니다.";
            }
            return "오픈 주문 한도 사용률이 임계 구간입니다.";
        }
        if ("WARN".equalsIgnoreCase(healthStatus)) {
            if (staleOrderCount > 0) {
                return "미체결 " + staleThresholdMinutes + "분 초과 주문이 존재합니다.";
            }
            return "오픈 주문 한도 사용률이 " + openOrderUsagePct + "% 입니다.";
        }
        return "현재 주문 건전성 상태가 정상입니다.";
    }

    private long orderAgeMinutes(Instant createdAt, Instant now) {
        if (createdAt == null || now == null) {
            return 0L;
        }
        return Math.max(0L, Duration.between(createdAt, now).toMinutes());
    }

    private void requireTradingEnabled(Long portfolioId) {
        RiskLimit riskLimit = ensureRiskLimit(portfolioId);
        if (Boolean.TRUE.equals(riskLimit.tradingEnabled())) {
            return;
        }

        String reason = riskLimit.killSwitchReason();
        String suffix = (reason == null || reason.isBlank()) ? "" : ", reason=" + reason;
        throw new IllegalArgumentException("trading is disabled by kill switch for portfolioId=" + portfolioId + suffix);
    }

    private BigDecimal bpsAmount(BigDecimal notional, BigDecimal bps) {
        return scale6(notional.multiply(bps).divide(BPS_DENOMINATOR, 6, RoundingMode.HALF_UP));
    }

    private BigDecimal remainingQuantity(Order order) {
        BigDecimal quantity = order.quantity() == null ? BigDecimal.ZERO : order.quantity();
        BigDecimal filled = order.filledQuantity() == null ? BigDecimal.ZERO : order.filledQuantity();
        BigDecimal remaining = quantity.subtract(filled);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return scale6(remaining);
    }

    private String normalizeFilterString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private int normalizeTradingControlHistoryLimit(Integer limit) {
        if (limit == null) {
            return 50;
        }
        int normalized = Math.max(1, limit);
        return Math.min(normalized, 500);
    }

    private String normalizeRiskAlertKey(String alertKey) {
        if (alertKey == null || alertKey.isBlank()) {
            throw new IllegalArgumentException("alertKey is required");
        }
        return alertKey.trim();
    }

    private String riskAlertAckKey(Long portfolioId, String alertKey) {
        return portfolioId + ":" + alertKey.toUpperCase(Locale.ROOT);
    }

    private String normalizeRiskAlertNote(String note) {
        if (note == null) {
            return null;
        }
        String normalized = note.trim();
        if (normalized.isBlank()) {
            return null;
        }
        if (normalized.length() > 200) {
            throw new IllegalArgumentException("risk alert note must be <= 200 chars");
        }
        return normalized;
    }

    private String normalizeRiskAlertWorkflowStatus(String workflowStatus) {
        String normalized = workflowStatus == null ? "OPEN" : workflowStatus.trim().toUpperCase(Locale.ROOT);
        if (normalized.isBlank()) {
            normalized = "OPEN";
        }
        if (!RISK_ALERT_WORKFLOW_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("invalid risk alert workflowStatus: " + workflowStatus);
        }
        return normalized;
    }

    private String normalizeRiskAlertAssignee(String assignee) {
        if (assignee == null) {
            return null;
        }
        String normalized = assignee.trim();
        if (normalized.isBlank()) {
            return null;
        }
        if (normalized.length() > 120) {
            throw new IllegalArgumentException("assignee must be <= 120 chars");
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    private int normalizeRecentMinutes(Integer recentMinutes) {
        if (recentMinutes == null) {
            return 180;
        }
        if (recentMinutes < 1 || recentMinutes > 1440) {
            throw new IllegalArgumentException("recentMinutes must be between 1 and 1440");
        }
        return recentMinutes;
    }

    private String normalizeAuditAction(String action) {
        if (action == null || action.isBlank()) {
            return "UNKNOWN";
        }
        return action.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeAuditStatus(OrderStatus status) {
        if (status == null) {
            return "-";
        }
        return status.name();
    }

    private String normalizeAuditActor(String actor) {
        if (actor == null || actor.isBlank()) {
            return SYSTEM_ACTOR;
        }
        return actor.trim().toLowerCase(Locale.ROOT);
    }

    private int resolveRiskAlertSlaTargetMinutes(String severity, String workflowStatus) {
        String normalizedSeverity = severity == null ? "INFO" : severity.trim().toUpperCase(Locale.ROOT);
        String normalizedWorkflow = normalizeRiskAlertWorkflowStatus(workflowStatus);
        if ("RESOLVED".equals(normalizedWorkflow)) {
            return switch (normalizedSeverity) {
                case "CRITICAL" -> SLA_CRITICAL_IN_PROGRESS_MINUTES;
                case "WARN" -> SLA_WARN_IN_PROGRESS_MINUTES;
                default -> SLA_INFO_IN_PROGRESS_MINUTES;
            };
        }
        boolean inProgress = "IN_PROGRESS".equals(normalizedWorkflow);
        return switch (normalizedSeverity) {
            case "CRITICAL" -> inProgress ? SLA_CRITICAL_IN_PROGRESS_MINUTES : SLA_CRITICAL_OPEN_MINUTES;
            case "WARN" -> inProgress ? SLA_WARN_IN_PROGRESS_MINUTES : SLA_WARN_OPEN_MINUTES;
            default -> inProgress ? SLA_INFO_IN_PROGRESS_MINUTES : SLA_INFO_OPEN_MINUTES;
        };
    }

    private boolean isRiskAlertSlaBreached(long ageMinutes, int slaTargetMinutes, String workflowStatus) {
        String normalizedWorkflow = normalizeRiskAlertWorkflowStatus(workflowStatus);
        if ("RESOLVED".equals(normalizedWorkflow)) {
            return false;
        }
        return ageMinutes > Math.max(1, slaTargetMinutes);
    }

    private int resolveRiskAlertPriorityScore(
            String severity,
            String workflowStatus,
            Boolean acknowledged,
            boolean slaBreached,
            long ageMinutes
    ) {
        String normalizedSeverity = severity == null ? "INFO" : severity.trim().toUpperCase(Locale.ROOT);
        String normalizedWorkflow = normalizeRiskAlertWorkflowStatus(workflowStatus);
        int score = switch (normalizedSeverity) {
            case "CRITICAL" -> 100;
            case "WARN" -> 70;
            default -> 40;
        };
        if (!Boolean.TRUE.equals(acknowledged)) {
            score += 10;
        }
        if ("IN_PROGRESS".equals(normalizedWorkflow)) {
            score += 5;
        } else if ("RESOLVED".equals(normalizedWorkflow)) {
            score -= 30;
        }
        if (slaBreached) {
            score += 25;
        }
        score += (int) Math.min(20L, ageMinutes / 30L);

        if (score < 0) {
            return 0;
        }
        if (score > 999) {
            return 999;
        }
        return score;
    }

    private long alertAgeMinutes(Instant occurredAt, Instant now) {
        if (occurredAt == null) {
            return 0L;
        }
        return Math.max(0L, Duration.between(occurredAt, now).toMinutes());
    }

    private int calculatePortfolioHealthScore(
            BigDecimal turnoverUsagePct,
            BigDecimal openOrderRatioPct,
            BigDecimal pnlMarginPct,
            int criticalAlertCount,
            int warnAlertCount,
            boolean tradingEnabled
    ) {
        int score = 100;

        if (turnoverUsagePct.compareTo(new BigDecimal("95")) >= 0) {
            score -= 35;
        } else if (turnoverUsagePct.compareTo(new BigDecimal("80")) >= 0) {
            score -= 20;
        } else if (turnoverUsagePct.compareTo(new BigDecimal("60")) >= 0) {
            score -= 10;
        }

        if (openOrderRatioPct.compareTo(new BigDecimal("70")) >= 0) {
            score -= 20;
        } else if (openOrderRatioPct.compareTo(new BigDecimal("45")) >= 0) {
            score -= 10;
        }

        if (pnlMarginPct.compareTo(new BigDecimal("-3")) <= 0) {
            score -= 30;
        } else if (pnlMarginPct.compareTo(new BigDecimal("-1")) <= 0) {
            score -= 15;
        } else if (pnlMarginPct.compareTo(new BigDecimal("2")) >= 0) {
            score += 5;
        }

        score -= Math.min(40, criticalAlertCount * 15);
        score -= Math.min(20, warnAlertCount * 5);

        if (!tradingEnabled) {
            score -= 25;
        }

        if (score < 0) {
            return 0;
        }
        if (score > 100) {
            return 100;
        }
        return score;
    }

    private String evaluatePortfolioHealthStatus(
            int healthScore,
            int criticalAlertCount,
            int warnAlertCount,
            boolean tradingEnabled
    ) {
        if (!tradingEnabled || criticalAlertCount > 0 || healthScore < 55) {
            return "CRITICAL";
        }
        if (warnAlertCount > 0 || healthScore < 75) {
            return "WARN";
        }
        return "HEALTHY";
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void validateQuantityRange(BigDecimal minValue, BigDecimal maxValue, String fieldName) {
        if (minValue != null && minValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " min must be >= 0");
        }
        if (maxValue != null && maxValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " max must be >= 0");
        }
        if (minValue != null && maxValue != null && minValue.compareTo(maxValue) > 0) {
            throw new IllegalArgumentException(fieldName + " min must be <= max");
        }
    }

    private void validateInstantRange(Instant from, Instant to, String fieldName) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException(fieldName + " from must be <= to");
        }
    }

    private BigDecimal scale6(BigDecimal value) {
        return value.setScale(6, RoundingMode.HALF_UP);
    }

    private String key(Long portfolioId, String symbol) {
        return portfolioId + ":" + symbol.toUpperCase();
    }
}
