package com.quant.mvp.pipeline.payload;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderWorkbenchPayload {

    private OrderWorkbenchPayload() {
    }

    public record Summary(
            Long portfolioId,
            Integer staleMinutes,
            Long totalOrderCount,
            Long openOrderCount,
            Long staleOrderCount,
            BigDecimal openNotional,
            BigDecimal dailyTurnover,
            BigDecimal turnoverUsagePct,
            BigDecimal totalPnl,
            Instant generatedAt
    ) {
    }

    public record StatusCounter(
            String status,
            Long count,
            BigDecimal estimatedNotional
    ) {
    }

    public record TopSymbol(
            String symbol,
            Long openOrderCount,
            BigDecimal openNotional,
            BigDecimal requestedQuantity,
            BigDecimal filledQuantity,
            BigDecimal fillRatePct
    ) {
    }

    public record StaleOrder(
            Long orderId,
            String symbol,
            String status,
            BigDecimal remainingQuantity,
            Long orderAgeMinutes,
            Instant createdAt
    ) {
    }

    public record Res(
            Summary summary,
            List<StatusCounter> statusCounters,
            List<TopSymbol> topSymbols,
            List<StaleOrder> staleOrders
    ) {
    }
}
