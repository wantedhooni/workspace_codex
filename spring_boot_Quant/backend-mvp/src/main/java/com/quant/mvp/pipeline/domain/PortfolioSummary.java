package com.quant.mvp.pipeline.domain;

import java.math.BigDecimal;

public record PortfolioSummary(
        Long portfolioId,
        BigDecimal grossExposure,
        BigDecimal marketValue,
        BigDecimal realizedPnl,
        BigDecimal unrealizedPnl,
        BigDecimal totalPnl,
        BigDecimal dailyTurnover,
        BigDecimal turnoverUsagePct,
        int openOrderCount,
        int filledOrderCount,
        int tradeCount,
        int positionCount
) {
}
