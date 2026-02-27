package com.quant.mvp.pipeline.payload;

import java.math.BigDecimal;
import java.util.List;

public final class PortfolioSummaryPayload {

    private PortfolioSummaryPayload() {
    }

    public record Item(
            Long portfolioId,
            BigDecimal grossExposure,
            BigDecimal marketValue,
            BigDecimal realizedPnl,
            BigDecimal unrealizedPnl,
            BigDecimal totalPnl,
            BigDecimal dailyTurnover,
            BigDecimal turnoverUsagePct,
            Integer openOrderCount,
            Integer filledOrderCount,
            Integer tradeCount,
            Integer positionCount
    ) {
    }

    public record Res(
            List<Item> items
    ) {
    }
}
