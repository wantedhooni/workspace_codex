package com.quant.mvp.api;

import com.quant.mvp.pipeline.domain.PermissionAction;
import com.quant.mvp.pipeline.payload.OrderWorkbenchPayload;
import com.quant.mvp.pipeline.service.OrderWorkbenchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders/workbench")
public class OrderWorkbenchController {

    private final OrderWorkbenchService orderWorkbenchService;
    private final PermissionGuard permissionGuard;

    public OrderWorkbenchController(
            OrderWorkbenchService orderWorkbenchService,
            PermissionGuard permissionGuard
    ) {
        this.orderWorkbenchService = orderWorkbenchService;
        this.permissionGuard = permissionGuard;
    }

    @GetMapping
    public OrderWorkbenchPayload.Res summary(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestParam(required = false) Long portfolioId,
            @RequestParam(required = false) Integer staleMinutes,
            @RequestParam(required = false) Integer topN
    ) {
        permissionGuard.require(userEmail, "orders", PermissionAction.READ);
        OrderWorkbenchService.OrderWorkbenchSnapshot snapshot = orderWorkbenchService.snapshot(
                portfolioId,
                staleMinutes,
                topN
        );
        return new OrderWorkbenchPayload.Res(
                new OrderWorkbenchPayload.Summary(
                        snapshot.summary().portfolioId(),
                        snapshot.summary().staleMinutes(),
                        snapshot.summary().totalOrderCount(),
                        snapshot.summary().openOrderCount(),
                        snapshot.summary().staleOrderCount(),
                        snapshot.summary().openNotional(),
                        snapshot.summary().dailyTurnover(),
                        snapshot.summary().turnoverUsagePct(),
                        snapshot.summary().totalPnl(),
                        snapshot.summary().generatedAt()
                ),
                snapshot.statusCounters().stream()
                        .map(row -> new OrderWorkbenchPayload.StatusCounter(
                                row.status(),
                                row.count(),
                                row.estimatedNotional()
                        ))
                        .toList(),
                snapshot.topSymbols().stream()
                        .map(row -> new OrderWorkbenchPayload.TopSymbol(
                                row.symbol(),
                                row.openOrderCount(),
                                row.openNotional(),
                                row.requestedQuantity(),
                                row.filledQuantity(),
                                row.fillRatePct()
                        ))
                        .toList(),
                snapshot.staleOrders().stream()
                        .map(row -> new OrderWorkbenchPayload.StaleOrder(
                                row.orderId(),
                                row.symbol(),
                                row.status(),
                                row.remainingQuantity(),
                                row.orderAgeMinutes(),
                                row.createdAt()
                        ))
                        .toList()
        );
    }
}
