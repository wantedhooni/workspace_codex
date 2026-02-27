package com.quant.mvp.pipeline.payload;

import java.time.Instant;

public final class CancelOrderPayload {

    private CancelOrderPayload() {
    }

    public record Req(
            String reason
    ) {
    }

    public record Res(
            Long orderId,
            String status,
            String reason,
            Instant decidedAt
    ) {
    }
}
