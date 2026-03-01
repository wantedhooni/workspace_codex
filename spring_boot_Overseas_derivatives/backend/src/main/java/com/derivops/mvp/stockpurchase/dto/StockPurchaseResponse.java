package com.derivops.mvp.stockpurchase.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record StockPurchaseResponse(
        Long id,
        Long accountId,
        String accountNo,
        String symbol,
        String market,
        String currency,
        LocalDate tradeDate,
        LocalDate settlementDate,
        BigDecimal quantity,
        BigDecimal price,
        BigDecimal grossAmount,
        BigDecimal feeAmount,
        BigDecimal netAmount,
        String brokerOrderNo,
        String createdBy,
        OffsetDateTime createdAt
) {
}
