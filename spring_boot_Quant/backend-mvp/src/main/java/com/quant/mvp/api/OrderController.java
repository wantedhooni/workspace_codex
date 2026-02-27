package com.quant.mvp.api;

import com.quant.mvp.pipeline.domain.Order;
import com.quant.mvp.pipeline.domain.OrderAuditLog;
import com.quant.mvp.pipeline.domain.OrderSide;
import com.quant.mvp.pipeline.domain.OrderStatus;
import com.quant.mvp.pipeline.domain.OrderType;
import com.quant.mvp.pipeline.domain.PermissionAction;
import com.quant.mvp.pipeline.payload.CancelOrderPayload;
import com.quant.mvp.pipeline.domain.TimeInForce;
import com.quant.mvp.pipeline.payload.BulkOrderActionPayload;
import com.quant.mvp.pipeline.payload.CreateOrderPayload;
import com.quant.mvp.pipeline.payload.DeleteOrderPayload;
import com.quant.mvp.pipeline.payload.OrderAuditPayload;
import com.quant.mvp.pipeline.payload.OrderAuditSummaryPayload;
import com.quant.mvp.pipeline.payload.OrderInsightPayload;
import com.quant.mvp.pipeline.payload.OrderListPayload;
import com.quant.mvp.pipeline.payload.RejectOrderPayload;
import com.quant.mvp.pipeline.service.OrderTradePositionPipelineService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderTradePositionPipelineService pipelineService;
    private final PermissionGuard permissionGuard;

    public OrderController(
            OrderTradePositionPipelineService pipelineService,
            PermissionGuard permissionGuard
    ) {
        this.pipelineService = pipelineService;
        this.permissionGuard = permissionGuard;
    }

    @GetMapping
    public OrderListPayload.Res list(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestParam(required = false) Long portfolioId,
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String side,
            @RequestParam(required = false) String orderType,
            @RequestParam(required = false) String timeInForce,
            @RequestParam(required = false) BigDecimal minQuantity,
            @RequestParam(required = false) BigDecimal maxQuantity,
            @RequestParam(required = false) BigDecimal minRemainingQuantity,
            @RequestParam(required = false) BigDecimal maxRemainingQuantity,
            @RequestParam(required = false) String createdFrom,
            @RequestParam(required = false) String createdTo
    ) {
        permissionGuard.require(userEmail, "orders", PermissionAction.READ);
        return new OrderListPayload.Res(
                pipelineService.searchOrders(
                                portfolioId,
                                symbol,
                                parseStatusFilter(status),
                                parseSideFilter(side),
                                parseOrderTypeFilter(orderType),
                                parseTimeInForceFilter(timeInForce),
                                minQuantity,
                                maxQuantity,
                                minRemainingQuantity,
                                maxRemainingQuantity,
                                parseInstantFilter(createdFrom, "createdFrom"),
                                parseInstantFilter(createdTo, "createdTo")
                        )
                        .stream()
                        .map(this::toItem)
                        .collect(Collectors.toList())
                );
    }

    @GetMapping("/{orderId}/insight")
    public OrderInsightPayload.Res insight(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @PathVariable Long orderId,
            @RequestParam(required = false) Integer staleMinutes
    ) {
        permissionGuard.require(userEmail, "orders", PermissionAction.READ);
        OrderTradePositionPipelineService.OrderInsightView insight = pipelineService.getOrderInsight(orderId, staleMinutes);
        return toInsightRes(insight);
    }

    @GetMapping("/audit-logs")
    public OrderAuditPayload.Res auditLogs(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Long portfolioId,
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String actor
    ) {
        permissionGuard.require(userEmail, "orderAudits", PermissionAction.READ);
        return new OrderAuditPayload.Res(
                pipelineService.searchOrderAudits(orderId, portfolioId, symbol, action, actor).stream()
                        .map(this::toAuditItem)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/audit-logs/summary")
    public OrderAuditSummaryPayload.Res auditSummary(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Long portfolioId,
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) Integer recentMinutes
    ) {
        permissionGuard.require(userEmail, "orderAudits", PermissionAction.READ);
        OrderTradePositionPipelineService.OrderAuditSummaryView summary = pipelineService.summarizeOrderAudits(
                orderId,
                portfolioId,
                symbol,
                action,
                actor,
                recentMinutes
        );
        return new OrderAuditSummaryPayload.Res(
                summary.portfolioId(),
                summary.recentMinutes(),
                summary.totalCount(),
                summary.recentCount(),
                summary.distinctOrderCount(),
                summary.lastActedAt(),
                summary.generatedAt(),
                summary.actionCounters().stream()
                        .map(item -> new OrderAuditSummaryPayload.ActionCounter(item.action(), item.count()))
                        .toList(),
                summary.transitionCounters().stream()
                        .map(item -> new OrderAuditSummaryPayload.TransitionCounter(
                                item.fromStatus(),
                                item.toStatus(),
                                item.count()))
                        .toList(),
                summary.topActors().stream()
                        .map(item -> new OrderAuditSummaryPayload.ActorCounter(item.actor(), item.count()))
                        .toList()
        );
    }

    @PostMapping
    public CreateOrderPayload.Res create(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @Valid @RequestBody CreateOrderPayload.Req req
    ) {
        permissionGuard.require(userEmail, "orders", PermissionAction.CREATE);
        String actor = permissionGuard.resolveUserEmail(userEmail);
        Order order = pipelineService.createOrder(
                req.portfolioId(),
                req.symbol(),
                parseSide(req.side()),
                req.quantity(),
                parseOrderType(req.orderType()),
                parseTimeInForce(req.timeInForce()),
                req.limitPrice(),
                actor
        );
        return toRes(order);
    }

    private OrderListPayload.Item toItem(Order order) {
        return new OrderListPayload.Item(
                order.orderId(),
                order.portfolioId(),
                order.symbol(),
                order.side().name(),
                order.orderType().name(),
                order.timeInForce().name(),
                order.limitPrice(),
                order.quantity(),
                order.filledQuantity(),
                remainingQuantity(order),
                fillRate(order),
                order.status(),
                order.createdAt(),
                order.decisionReason(),
                order.decidedAt()
        );
    }

    private OrderAuditPayload.Item toAuditItem(OrderAuditLog auditLog) {
        return new OrderAuditPayload.Item(
                auditLog.auditId(),
                auditLog.orderId(),
                auditLog.portfolioId(),
                auditLog.symbol(),
                auditLog.action(),
                auditLog.fromStatus(),
                auditLog.toStatus(),
                auditLog.reason(),
                auditLog.actor(),
                auditLog.actedAt()
        );
    }

    @DeleteMapping("/{orderId}")
    public DeleteOrderPayload.Res delete(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @PathVariable Long orderId
    ) {
        permissionGuard.require(userEmail, "orders", PermissionAction.DELETE);
        String actor = permissionGuard.resolveUserEmail(userEmail);
        Order deleted = pipelineService.deleteOrder(orderId, actor);
        return new DeleteOrderPayload.Res(
                deleted.orderId(),
                deleted.status().name(),
                Instant.now()
        );
    }

    @PostMapping("/{orderId}/cancel")
    public CancelOrderPayload.Res cancel(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @PathVariable Long orderId,
            @RequestBody(required = false) CancelOrderPayload.Req req
    ) {
        permissionGuard.require(userEmail, "orders", PermissionAction.UPDATE);
        String actor = permissionGuard.resolveUserEmail(userEmail);
        String reason = req == null ? null : req.reason();
        Order canceled = pipelineService.cancelOrder(orderId, reason, actor);
        return new CancelOrderPayload.Res(
                canceled.orderId(),
                canceled.status().name(),
                canceled.decisionReason(),
                canceled.decidedAt()
        );
    }

    @PostMapping("/{orderId}/reject")
    public RejectOrderPayload.Res reject(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @PathVariable Long orderId,
            @Valid @RequestBody RejectOrderPayload.Req req
    ) {
        permissionGuard.require(userEmail, "orders", PermissionAction.UPDATE);
        String actor = permissionGuard.resolveUserEmail(userEmail);
        Order rejected = pipelineService.rejectOrder(orderId, req.reason(), actor);
        return new RejectOrderPayload.Res(
                rejected.orderId(),
                rejected.status().name(),
                rejected.decisionReason(),
                rejected.decidedAt()
        );
    }

    @PostMapping("/bulk/cancel")
    public BulkOrderActionPayload.Res bulkCancel(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @Valid @RequestBody BulkOrderActionPayload.Req req
    ) {
        permissionGuard.require(userEmail, "orders", PermissionAction.UPDATE);
        String actor = permissionGuard.resolveUserEmail(userEmail);
        return doBulkAction("CANCEL", req, actor, true);
    }

    @PostMapping("/bulk/reject")
    public BulkOrderActionPayload.Res bulkReject(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @Valid @RequestBody BulkOrderActionPayload.Req req
    ) {
        permissionGuard.require(userEmail, "orders", PermissionAction.UPDATE);
        String actor = permissionGuard.resolveUserEmail(userEmail);
        return doBulkAction("REJECT", req, actor, false);
    }

    private CreateOrderPayload.Res toRes(Order order) {
        return new CreateOrderPayload.Res(
                order.orderId(),
                order.portfolioId(),
                order.symbol(),
                order.side().name(),
                order.orderType().name(),
                order.timeInForce().name(),
                order.limitPrice(),
                order.quantity(),
                order.filledQuantity(),
                remainingQuantity(order),
                fillRate(order),
                order.status(),
                order.createdAt(),
                order.decisionReason(),
                order.decidedAt()
        );
    }

    private OrderSide parseSide(String side) {
        if (side == null || side.isBlank()) {
            throw new IllegalArgumentException("side is required");
        }
        try {
            return OrderSide.valueOf(side.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("invalid side: " + side + " (allowed: BUY, SELL)");
        }
    }

    private OrderType parseOrderType(String orderType) {
        if (orderType == null || orderType.isBlank()) {
            return null;
        }
        try {
            return OrderType.valueOf(orderType.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("invalid orderType: " + orderType + " (allowed: MARKET, LIMIT)");
        }
    }

    private TimeInForce parseTimeInForce(String timeInForce) {
        if (timeInForce == null || timeInForce.isBlank()) {
            return null;
        }
        try {
            return TimeInForce.valueOf(timeInForce.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("invalid timeInForce: " + timeInForce + " (allowed: DAY, GTC, IOC)");
        }
    }

    private String parseStatusFilter(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return OrderStatus.valueOf(status.trim().toUpperCase()).name();
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("invalid status: " + status + " (allowed: NEW, SENT, PARTIAL, FILLED, CANCELED, REJECTED)");
        }
    }

    private String parseSideFilter(String side) {
        if (side == null || side.isBlank()) {
            return null;
        }
        try {
            return OrderSide.valueOf(side.trim().toUpperCase()).name();
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("invalid side: " + side + " (allowed: BUY, SELL)");
        }
    }

    private String parseOrderTypeFilter(String orderType) {
        if (orderType == null || orderType.isBlank()) {
            return null;
        }
        try {
            return OrderType.valueOf(orderType.trim().toUpperCase()).name();
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("invalid orderType: " + orderType + " (allowed: MARKET, LIMIT)");
        }
    }

    private String parseTimeInForceFilter(String timeInForce) {
        if (timeInForce == null || timeInForce.isBlank()) {
            return null;
        }
        try {
            return TimeInForce.valueOf(timeInForce.trim().toUpperCase()).name();
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("invalid timeInForce: " + timeInForce + " (allowed: DAY, GTC, IOC)");
        }
    }

    private Instant parseInstantFilter(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(fieldName + " must be ISO-8601 instant format, e.g. 2026-02-10T00:00:00Z");
        }
    }

    private BigDecimal remainingQuantity(Order order) {
        return order.quantity().subtract(order.filledQuantity()).setScale(6, RoundingMode.HALF_UP);
    }

    private BigDecimal fillRate(Order order) {
        if (order.quantity().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return order.filledQuantity()
                .divide(order.quantity(), 6, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BulkOrderActionPayload.Res doBulkAction(
            String action,
            BulkOrderActionPayload.Req req,
            String actor,
            boolean cancel
    ) {
        List<Long> requestedOrderIds = new ArrayList<>(new LinkedHashSet<>(req.orderIds()));
        List<BulkOrderActionPayload.Item> items = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;

        for (Long orderId : requestedOrderIds) {
            try {
                Order updated = cancel
                        ? pipelineService.cancelOrder(orderId, req.reason(), actor)
                        : pipelineService.rejectOrder(orderId, req.reason(), actor);

                items.add(new BulkOrderActionPayload.Item(
                        updated.orderId(),
                        true,
                        updated.status().name(),
                        updated.decisionReason(),
                        updated.decidedAt(),
                        "OK"
                ));
                successCount += 1;
            } catch (RuntimeException ex) {
                items.add(new BulkOrderActionPayload.Item(
                        orderId,
                        false,
                        null,
                        req.reason(),
                        null,
                        ex.getMessage()
                ));
                failedCount += 1;
            }
        }

        return new BulkOrderActionPayload.Res(
                action,
                requestedOrderIds.size(),
                successCount,
                failedCount,
                items,
                Instant.now()
        );
    }

    private OrderInsightPayload.Res toInsightRes(OrderTradePositionPipelineService.OrderInsightView insight) {
        Order order = insight.order();
        return new OrderInsightPayload.Res(
                order.orderId(),
                order.portfolioId(),
                order.symbol(),
                order.side().name(),
                order.orderType().name(),
                order.timeInForce().name(),
                order.status().name(),
                order.quantity(),
                order.filledQuantity(),
                insight.remainingQuantity(),
                insight.fillRatePct(),
                insight.requestedNotional(),
                insight.executedNotional(),
                insight.averageExecutionPrice(),
                insight.totalFee(),
                insight.totalSlippage(),
                insight.netCashFlow(),
                insight.staleMinutes(),
                insight.cancelable(),
                insight.rejectable(),
                order.decisionReason(),
                order.createdAt(),
                order.decidedAt(),
                insight.trades().stream()
                        .map(trade -> new OrderInsightPayload.TradeItem(
                                trade.tradeId(),
                                trade.tradeQuantity(),
                                trade.tradePrice(),
                                trade.notional(),
                                trade.fee(),
                                trade.slippage(),
                                trade.netCashFlow(),
                                trade.tradedAt()
                        ))
                        .toList(),
                insight.audits().stream()
                        .map(audit -> new OrderInsightPayload.AuditItem(
                                audit.auditId(),
                                audit.action(),
                                audit.fromStatus() == null ? null : audit.fromStatus().name(),
                                audit.toStatus() == null ? null : audit.toStatus().name(),
                                audit.reason(),
                                audit.actor(),
                                audit.actedAt()
                        ))
                        .toList(),
                insight.riskAlerts().stream()
                        .map(alert -> new OrderInsightPayload.RiskItem(
                                alert.severity(),
                                alert.code(),
                                alert.message(),
                                alert.metricName(),
                                alert.metricValue(),
                                alert.thresholdValue(),
                                alert.occurredAt()
                        ))
                        .toList()
        );
    }
}
