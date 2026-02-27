package com.tradingmacro.trade;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public record TradeRequest(
    @NotBlank String symbol,
    @NotNull Trade.Side side,
    @NotNull BigDecimal quantity,
    @NotNull BigDecimal price,
    Instant executedAt,
    Long strategyId,
    Long portfolioId
) {}
