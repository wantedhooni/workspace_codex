package com.quant.mvp.pipeline.payload;

import java.time.Instant;
import java.util.List;

public final class AccountSessionPayload {

    private AccountSessionPayload() {
    }

    public record Item(
            Long sessionId,
            String ipAddress,
            String userAgent,
            boolean active,
            Instant createdAt,
            Instant lastAccessAt
    ) {
    }

    public record Res(
            List<Item> items
    ) {
    }
}
