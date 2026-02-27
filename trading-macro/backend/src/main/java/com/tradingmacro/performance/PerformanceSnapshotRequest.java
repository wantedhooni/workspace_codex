package com.tradingmacro.performance;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PerformanceSnapshotRequest(
    @NotNull LocalDate snapshotDate,
    @NotNull BigDecimal pnl,
    @NotNull BigDecimal returnPct,
    @NotNull BigDecimal drawdownPct,
    @NotNull BigDecimal sharpeRatio,
    @NotNull BigDecimal volatilityPct,
    Long portfolioId
) {}
