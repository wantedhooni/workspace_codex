package com.quant.mvp.pipeline.domain;

import java.time.Instant;
import java.util.List;

public record JournalVoucher(
        Long voucherId,
        String voucherNo,
        Long portfolioId,
        Long tradeId,
        VoucherStatus status,
        List<JournalEntryLine> entries,
        String description,
        Instant createdAt,
        Instant approvedAt,
        Instant postedAt
) {

    public JournalVoucher approve() {
        return new JournalVoucher(
                voucherId,
                voucherNo,
                portfolioId,
                tradeId,
                VoucherStatus.APPROVED,
                entries,
                description,
                createdAt,
                Instant.now(),
                postedAt
        );
    }

    public JournalVoucher post() {
        return new JournalVoucher(
                voucherId,
                voucherNo,
                portfolioId,
                tradeId,
                VoucherStatus.POSTED,
                entries,
                description,
                createdAt,
                approvedAt,
                Instant.now()
        );
    }

    public JournalVoucher cancel() {
        return new JournalVoucher(
                voucherId,
                voucherNo,
                portfolioId,
                tradeId,
                VoucherStatus.CANCELED,
                entries,
                description,
                createdAt,
                approvedAt,
                postedAt
        );
    }
}
