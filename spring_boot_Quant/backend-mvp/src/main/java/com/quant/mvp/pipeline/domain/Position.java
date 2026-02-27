package com.quant.mvp.pipeline.domain;

import java.math.BigDecimal;

public record Position(
        Long portfolioId,
        String symbol,
        BigDecimal quantity,
        BigDecimal avgPrice,
        BigDecimal lastPrice,
        BigDecimal marketValue,
        BigDecimal unrealizedPnl,
        BigDecimal realizedPnl
) {
    public static Position empty(Long portfolioId, String symbol) {
        return new Position(
                portfolioId,
                symbol,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }
}
