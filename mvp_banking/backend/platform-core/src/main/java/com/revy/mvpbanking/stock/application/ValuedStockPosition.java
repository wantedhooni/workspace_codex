package com.revy.mvpbanking.stock.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ValuedStockPosition(
        UUID id,
        UUID customerId,
        UUID accountId,
        String symbol,
        String market,
        BigDecimal quantity,
        BigDecimal averagePrice,
        String currency,
        BigDecimal costBasis,
        BigDecimal currentPrice,
        BigDecimal marketValue,
        BigDecimal unrealizedProfitLoss,
        BigDecimal unrealizedProfitRate,
        BigDecimal realizedProfitLoss,
        BigDecimal changeRate,
        Instant quoteEffectiveAt,
        String quoteSource,
        Instant updatedAt
) {
}
