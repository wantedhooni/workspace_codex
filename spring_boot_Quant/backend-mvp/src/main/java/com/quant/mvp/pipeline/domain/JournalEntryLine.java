package com.quant.mvp.pipeline.domain;

import java.math.BigDecimal;

public record JournalEntryLine(
        int lineNo,
        String accountCode,
        DrCr drCr,
        BigDecimal amount,
        String symbol,
        String description
) {
}
