package com.quant.mvp.api;

import com.quant.mvp.pipeline.domain.PermissionAction;
import com.quant.mvp.pipeline.payload.OrderHealthPayload;
import com.quant.mvp.pipeline.payload.RemediateStaleOrderPayload;
import com.quant.mvp.pipeline.service.OrderTradePositionPipelineService;
import jakarta.validation.Valid;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order-health")
public class OrderHealthController {

    private final OrderTradePositionPipelineService pipelineService;
    private final PermissionGuard permissionGuard;

    public OrderHealthController(
            OrderTradePositionPipelineService pipelineService,
            PermissionGuard permissionGuard
    ) {
        this.pipelineService = pipelineService;
        this.permissionGuard = permissionGuard;
    }

    @GetMapping
    public OrderHealthPayload.Res list(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestParam(required = false) Long portfolioId,
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) Integer staleMinutes
    ) {
        permissionGuard.require(userEmail, "orderHealth", PermissionAction.READ);
        return new OrderHealthPayload.Res(
                pipelineService.searchOrderHealth(portfolioId, symbol, staleMinutes).stream()
                        .map(this::toItem)
                        .collect(Collectors.toList())
        );
    }

    @PostMapping("/remediate-stale")
    public RemediateStaleOrderPayload.Res remediateStale(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @Valid @RequestBody RemediateStaleOrderPayload.Req req
    ) {
        permissionGuard.require(userEmail, "orderHealth", PermissionAction.UPDATE);
        String actor = permissionGuard.resolveUserEmail(userEmail);
        OrderTradePositionPipelineService.StaleOrderRemediationResult result =
                pipelineService.remediateStaleOrders(
                        req.portfolioId(),
                        req.symbol(),
                        req.staleMinutes(),
                        req.reason(),
                        actor
                );

        return new RemediateStaleOrderPayload.Res(
                result.portfolioId(),
                result.symbol(),
                result.staleThresholdMinutes(),
                result.evaluatedOpenOrderCount(),
                result.staleOrderCount(),
                result.canceledCount(),
                result.items().stream().map(item -> new RemediateStaleOrderPayload.Item(
                        item.orderId(),
                        item.portfolioId(),
                        item.symbol(),
                        item.previousStatus(),
                        item.currentStatus(),
                        item.orderAgeMinutes(),
                        item.reason(),
                        item.decidedAt()
                )).collect(Collectors.toList()),
                result.executedAt()
        );
    }

    private OrderHealthPayload.Item toItem(OrderTradePositionPipelineService.OrderHealthView row) {
        return new OrderHealthPayload.Item(
                row.healthKey(),
                row.portfolioId(),
                row.symbol(),
                row.openOrderCount(),
                row.staleOrderCount(),
                row.staleThresholdMinutes(),
                row.openOrderUsagePct(),
                row.averageOpenAgeMinutes(),
                row.maxOpenAgeMinutes(),
                row.oldestOpenOrderId(),
                row.healthStatus(),
                row.healthNote(),
                row.updatedAt()
        );
    }
}
