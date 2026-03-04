package com.revy.mvpbanking.stock.presentation;

import com.revy.mvpbanking.stock.application.StockOrderDetail;
import com.revy.mvpbanking.stock.domain.StockOrder;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record StockOrderResponse(
        UUID id,
        UUID customerId,
        UUID accountId,
        String orderNumber,
        String symbol,
        String market,
        String side,
        BigDecimal quantity,
        BigDecimal limitPrice,
        BigDecimal grossAmount,
        String currency,
        String status,
        BigDecimal executedQuantity,
        BigDecimal executedPrice,
        BigDecimal remainingQuantity,
        BigDecimal fillRate,
        BigDecimal feeAmount,
        BigDecimal taxAmount,
        BigDecimal netSettlementAmount,
        String settlementTransactionNumber,
        List<StockOrderExecutionResponse> executions,
        Instant settledAt,
        Instant createdAt
) {
    public static StockOrderResponse from(StockOrder stockOrder) {
        return new StockOrderResponse(
                stockOrder.getId(),
                stockOrder.getCustomerId(),
                stockOrder.getAccountId(),
                stockOrder.getOrderNumber(),
                stockOrder.getSymbol(),
                stockOrder.getMarket(),
                stockOrder.getSide().name(),
                stockOrder.getQuantity(),
                stockOrder.getLimitPrice(),
                stockOrder.getGrossAmount(),
                stockOrder.getCurrency(),
                stockOrder.getStatus().name(),
                stockOrder.getExecutedQuantity(),
                stockOrder.getExecutedPrice(),
                stockOrder.getRemainingQuantity(),
                resolveFillRate(stockOrder),
                stockOrder.getFeeAmount(),
                stockOrder.getTaxAmount(),
                stockOrder.getNetSettlementAmount(),
                stockOrder.getSettlementTransactionNumber(),
                List.of(),
                stockOrder.getSettledAt(),
                stockOrder.getCreatedAt()
        );
    }

    public static StockOrderResponse from(StockOrderDetail stockOrderDetail) {
        StockOrder stockOrder = stockOrderDetail.order();
        return new StockOrderResponse(
                stockOrder.getId(),
                stockOrder.getCustomerId(),
                stockOrder.getAccountId(),
                stockOrder.getOrderNumber(),
                stockOrder.getSymbol(),
                stockOrder.getMarket(),
                stockOrder.getSide().name(),
                stockOrder.getQuantity(),
                stockOrder.getLimitPrice(),
                stockOrder.getGrossAmount(),
                stockOrder.getCurrency(),
                stockOrder.getStatus().name(),
                stockOrder.getExecutedQuantity(),
                stockOrder.getExecutedPrice(),
                stockOrder.getRemainingQuantity(),
                resolveFillRate(stockOrder),
                stockOrder.getFeeAmount(),
                stockOrder.getTaxAmount(),
                stockOrder.getNetSettlementAmount(),
                stockOrder.getSettlementTransactionNumber(),
                stockOrderDetail.executions().stream().map(StockOrderExecutionResponse::from).toList(),
                stockOrder.getSettledAt(),
                stockOrder.getCreatedAt()
        );
    }

    private static BigDecimal resolveFillRate(StockOrder stockOrder) {
        if (stockOrder.getExecutedQuantity() == null || stockOrder.getQuantity() == null || stockOrder.getQuantity().signum() == 0) {
            return BigDecimal.ZERO;
        }
        return stockOrder.getExecutedQuantity().divide(stockOrder.getQuantity(), 4, java.math.RoundingMode.HALF_UP);
    }
}
