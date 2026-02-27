package com.quant.mvp.pipeline.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.Instant;
import java.util.List;

public final class BulkOrderActionPayload {

    private BulkOrderActionPayload() {
    }

    public record Req(
            @NotEmpty(message = "orderIds is required")
            List<Long> orderIds,
            @NotBlank(message = "reason is required")
            String reason
    ) {
    }

    public record Item(
            Long orderId,
            Boolean success,
            String status,
            String reason,
            Instant decidedAt,
            String message
    ) {
    }

    public record Res(
            String action,
            Integer requestedCount,
            Integer successCount,
            Integer failedCount,
            List<Item> items,
            Instant executedAt
    ) {
    }
}
