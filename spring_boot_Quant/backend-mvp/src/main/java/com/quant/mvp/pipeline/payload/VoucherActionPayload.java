package com.quant.mvp.pipeline.payload;

import com.quant.mvp.pipeline.domain.VoucherStatus;
import java.time.Instant;

public final class VoucherActionPayload {

    private VoucherActionPayload() {
    }

    public record Req(
            String reason
    ) {
    }

    public record Res(
            Long voucherId,
            String voucherNo,
            VoucherStatus status,
            Instant approvedAt,
            Instant postedAt
    ) {
    }
}
