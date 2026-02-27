package com.quant.mvp.pipeline.payload;

import java.time.Instant;

public final class ResetUserPasswordPayload {

    private ResetUserPasswordPayload() {
    }

    public record Req(
            String reason
    ) {
    }

    public record Res(
            Long userId,
            String temporaryPassword,
            Instant resetAt,
            String resetToken,
            Instant expiresAt
    ) {
    }
}
