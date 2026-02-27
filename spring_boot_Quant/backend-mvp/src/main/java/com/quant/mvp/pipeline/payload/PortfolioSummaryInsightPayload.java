package com.quant.mvp.pipeline.payload;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class PortfolioSummaryInsightPayload {

    private PortfolioSummaryInsightPayload() {
    }

    public record TopExposure(
            String symbol,
            BigDecimal quantity,
            BigDecimal marketValue,
            BigDecimal grossExposureWeightPct,
            BigDecimal totalPnl
    ) {
    }

    public record Item(
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
            List<TopExposure> topExposures
    ) {
    }

    public record Res(
            List<Item> items
    ) {
    }
}
