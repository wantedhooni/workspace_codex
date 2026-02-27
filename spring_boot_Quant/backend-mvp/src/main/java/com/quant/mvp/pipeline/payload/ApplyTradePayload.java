package com.quant.mvp.pipeline.payload;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;

public final class ApplyTradePayload {

    private ApplyTradePayload() {
    }

    public record Req(
            @NotNull Long orderId,
            @NotNull @Positive BigDecimal tradeQuantity,
            @NotNull @Positive BigDecimal tradePrice
    ) {
    }

    public record Res(
            Long tradeId,
            Long orderId,
            String symbol,
            String side,
            BigDecimal tradeQuantity,
            BigDecimal tradePrice,
            BigDecimal notional,
            BigDecimal fee,
            BigDecimal slippage,
            BigDecimal netCashFlow,
            Instant tradedAt
    ) {
    }
}
