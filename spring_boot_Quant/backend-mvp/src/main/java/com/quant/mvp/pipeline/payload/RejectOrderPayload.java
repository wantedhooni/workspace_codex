package com.quant.mvp.pipeline.payload;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public final class RejectOrderPayload {

    private RejectOrderPayload() {
    }

    public record Req(
            @NotBlank String reason
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
