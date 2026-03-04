package com.revy.mvpbanking.stock.presentation;

import com.revy.mvpbanking.stock.application.ValuedStockPosition;
import com.revy.mvpbanking.stock.domain.StockPosition;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StockPositionResponse(
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
    public static StockPositionResponse from(StockPosition stockPosition) {
        return new StockPositionResponse(
                stockPosition.getId(),
                stockPosition.getCustomerId(),
                stockPosition.getAccountId(),
                stockPosition.getSymbol(),
                stockPosition.getMarket(),
                stockPosition.getQuantity(),
                stockPosition.getAveragePrice(),
                stockPosition.getCurrency(),
                stockPosition.getAveragePrice().multiply(stockPosition.getQuantity()),
                null,
                null,
                null,
                null,
                stockPosition.getRealizedProfitLoss(),
                null,
                null,
                null,
                stockPosition.getUpdatedAt()
        );
    }

    public static StockPositionResponse from(ValuedStockPosition valuedPosition) {
        return new StockPositionResponse(
                valuedPosition.id(),
                valuedPosition.customerId(),
                valuedPosition.accountId(),
                valuedPosition.symbol(),
                valuedPosition.market(),
                valuedPosition.quantity(),
                valuedPosition.averagePrice(),
                valuedPosition.currency(),
                valuedPosition.costBasis(),
                valuedPosition.currentPrice(),
                valuedPosition.marketValue(),
                valuedPosition.unrealizedProfitLoss(),
                valuedPosition.unrealizedProfitRate(),
                valuedPosition.realizedProfitLoss(),
                valuedPosition.changeRate(),
                valuedPosition.quoteEffectiveAt(),
                valuedPosition.quoteSource(),
                valuedPosition.updatedAt()
        );
    }
}
