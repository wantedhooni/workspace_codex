package com.quant.mvp.pipeline.payload;

import java.time.Instant;
import java.util.List;

public final class AccountActivityPayload {

    private AccountActivityPayload() {
    }

    public record Req(
            Long portfolioId,
            Integer limit
    ) {
    }

    public record Item(
            String activityKey,
            String category,
            String severity,
            String title,
            String description,
            String path,
            String actor,
            Instant occurredAt
    ) {
    }

    public record Res(
            Req req,
            List<Item> items
    ) {
    }
}
