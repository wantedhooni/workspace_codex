package com.tradingmacro.risk;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public record RiskPolicyRequest(
    @NotBlank String name,
    @NotNull BigDecimal maxDailyLoss,
    @NotNull BigDecimal maxPositionSize,
    @NotNull BigDecimal maxLeverage,
    String allowedAssetClasses,
    @NotNull RiskPolicy.Status status,
    Instant updatedAt,
    Long portfolioId
) {}
