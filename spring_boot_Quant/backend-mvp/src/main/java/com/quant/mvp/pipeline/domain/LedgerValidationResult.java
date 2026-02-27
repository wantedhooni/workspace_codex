package com.quant.mvp.pipeline.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record LedgerValidationResult(
        Instant validatedAt,
        int postedVoucherCount,
        BigDecimal totalDebit,
        BigDecimal totalCredit,
        boolean balanced,
        String message
) {
}
