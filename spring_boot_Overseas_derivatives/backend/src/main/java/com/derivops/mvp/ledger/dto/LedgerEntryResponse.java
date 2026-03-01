package com.derivops.mvp.ledger.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record LedgerEntryResponse(
        Long id,
        String entryNo,
        Long accountId,
        String accountNo,
        String referenceType,
        String referenceId,
        String symbol,
        LocalDate postingDate,
        String currency,
        BigDecimal quantityChange,
        BigDecimal amountChange,
        BigDecimal runningQuantity,
        BigDecimal runningAmount,
        String description,
        OffsetDateTime createdAt
) {
}
