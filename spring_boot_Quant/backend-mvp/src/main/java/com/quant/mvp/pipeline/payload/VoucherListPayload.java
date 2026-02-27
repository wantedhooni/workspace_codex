package com.quant.mvp.pipeline.payload;

import com.quant.mvp.pipeline.domain.VoucherStatus;
import java.time.Instant;
import java.util.List;

public final class VoucherListPayload {

    private VoucherListPayload() {
    }

    public record Item(
            Long voucherId,
            String voucherNo,
            Long portfolioId,
            Long tradeId,
            VoucherStatus status,
            Instant createdAt,
            Instant approvedAt,
            Instant postedAt
    ) {
    }

    public record Res(
            List<Item> items
    ) {
    }
}
