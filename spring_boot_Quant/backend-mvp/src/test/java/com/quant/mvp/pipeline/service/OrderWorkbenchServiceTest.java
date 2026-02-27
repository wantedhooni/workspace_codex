package com.quant.mvp.pipeline.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.quant.mvp.pipeline.domain.Order;
import com.quant.mvp.pipeline.domain.OrderSide;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OrderWorkbenchServiceTest {

    @Test
    void workbenchAggregatesOpenAndStaleOrders() {
        OrderTradePositionPipelineService pipelineService = new OrderTradePositionPipelineService();
        OrderWorkbenchService workbenchService = new OrderWorkbenchService(pipelineService);

        Order first = pipelineService.createOrder(1L, "AAPL", OrderSide.BUY, new BigDecimal("10"));
        pipelineService.createOrder(1L, "MSFT", OrderSide.BUY, new BigDecimal("5"));
        pipelineService.applyTrade(first.orderId(), new BigDecimal("10"), new BigDecimal("180"));

        OrderWorkbenchService.OrderWorkbenchSnapshot snapshot = workbenchService.snapshot(1L, 0, 5);

        assertEquals(2L, snapshot.summary().totalOrderCount());
        assertEquals(1L, snapshot.summary().openOrderCount());
        assertEquals(1L, snapshot.summary().staleOrderCount());
        assertTrue(snapshot.statusCounters().stream().anyMatch(row -> "FILLED".equals(row.status())));
        assertTrue(snapshot.statusCounters().stream().anyMatch(row -> "NEW".equals(row.status())));
        assertTrue(snapshot.topSymbols().stream().anyMatch(row ->
                "MSFT".equals(row.symbol()) && row.openOrderCount() == 1L));
    }
}
