package com.revy.mvpbanking.stock.presentation;

import com.revy.mvpbanking.stock.domain.StockOrderExecution;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StockOrderExecutionResponse(
        UUID id,
        String executionNumber,
        Integer executionSequence,
        BigDecimal executedQuantity,
        BigDecimal executedPrice,
        BigDecimal executedAmount,
        Instant executedAt
) {
    public static StockOrderExecutionResponse from(StockOrderExecution stockOrderExecution) {
        return new StockOrderExecutionResponse(
                stockOrderExecution.getId(),
                stockOrderExecution.getExecutionNumber(),
                stockOrderExecution.getExecutionSequence(),
                stockOrderExecution.getExecutedQuantity(),
                stockOrderExecution.getExecutedPrice(),
                stockOrderExecution.getExecutedAmount(),
                stockOrderExecution.getExecutedAt()
        );
    }
}
