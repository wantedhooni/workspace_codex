package com.quant.mvp.pipeline.payload;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class TradeListPayload {

    private TradeListPayload() {
    }

    public record Item(
            Long tradeId,
            Long orderId,
            Long portfolioId,
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

    public record Res(
            List<Item> items
    ) {
    }
}
