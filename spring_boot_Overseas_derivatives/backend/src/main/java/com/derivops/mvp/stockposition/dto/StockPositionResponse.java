package com.derivops.mvp.stockposition.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record StockPositionResponse(
        Long id,
        Long accountId,
        String accountNo,
        String symbol,
        String market,
        String currency,
        BigDecimal quantity,
        BigDecimal averagePrice,
        BigDecimal totalCost,
        LocalDate lastTradeDate,
        OffsetDateTime updatedAt
) {
}
