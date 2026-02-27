package com.quant.mvp.pipeline.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record Trade(
        Long tradeId,
        Long orderId,
        String symbol,
        OrderSide side,
        BigDecimal tradeQuantity,
        BigDecimal tradePrice,
        BigDecimal notional,
        BigDecimal fee,
        BigDecimal slippage,
        BigDecimal netCashFlow,
        Instant tradedAt
) {
}
