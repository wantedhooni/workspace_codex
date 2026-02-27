package com.quant.mvp.pipeline.payload;

import com.quant.mvp.pipeline.domain.DrCr;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class LedgerEntrySearchPayload {

    private LedgerEntrySearchPayload() {
    }

    public record Req(
            Long portfolioId,
            Long voucherId
    ) {
    }

    public record Item(
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

    public record Res(
            List<Item> items
    ) {
    }
}
