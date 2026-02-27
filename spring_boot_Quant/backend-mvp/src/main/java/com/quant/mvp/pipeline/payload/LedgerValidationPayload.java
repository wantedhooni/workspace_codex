package com.quant.mvp.pipeline.payload;

import java.math.BigDecimal;
import java.time.Instant;

public final class LedgerValidationPayload {

    private LedgerValidationPayload() {
    }

    public record Res(
            Instant validatedAt,
            int postedVoucherCount,
            BigDecimal totalDebit,
            BigDecimal totalCredit,
            boolean balanced,
            String message
    ) {
    }
}
