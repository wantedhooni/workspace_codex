package com.quant.mvp.pipeline.payload;

import java.time.Instant;

public final class RevokeSessionPayload {

    private RevokeSessionPayload() {
    }

    public record Req(
            String reason
    ) {
    }

    public record Res(
            Long sessionId,
            boolean active,
            Instant updatedAt
    ) {
    }
}
