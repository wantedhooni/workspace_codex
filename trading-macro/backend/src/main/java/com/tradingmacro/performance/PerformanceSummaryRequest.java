package com.tradingmacro.performance;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PerformanceSummaryRequest(
    @NotNull PerformanceSummary.Period period,
    @NotNull LocalDate periodStart,
    @NotNull LocalDate periodEnd,
    @NotNull BigDecimal returnPct,
    @NotNull BigDecimal benchmarkReturnPct,
    @NotNull BigDecimal excessReturnPct,
    @NotNull BigDecimal maxDrawdownPct,
    @NotNull BigDecimal winRatePct,
    @NotNull BigDecimal profitFactor,
    String benchmarkName,
    Long portfolioId,
    Long strategyId
) {}
