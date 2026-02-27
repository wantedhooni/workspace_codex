package com.quant.mvp.pipeline.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record LedgerEntry(
        Long ledgerEntryId,
        Long voucherId,
        Long portfolioId,
        String accountCode,
        DrCr drCr,
        BigDecimal amount,
        String symbol,
        String description,
        Instant createdAt
) {
}
