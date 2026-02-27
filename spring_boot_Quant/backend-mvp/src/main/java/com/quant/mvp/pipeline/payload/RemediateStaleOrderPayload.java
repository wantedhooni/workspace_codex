package com.quant.mvp.pipeline.payload;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.List;

public final class RemediateStaleOrderPayload {

    private RemediateStaleOrderPayload() {
    }

    public record Req(
            @NotNull @Positive Long portfolioId,
            String symbol,
            @Min(0) Integer staleMinutes,
            String reason
    ) {
    }

    public record Item(
            Long orderId,
            Long portfolioId,
            String symbol,
            String previousStatus,
            String currentStatus,
            Long orderAgeMinutes,
            String reason,
            Instant decidedAt
    ) {
    }

    public record Res(
            Long portfolioId,
            String symbol,
            Integer staleThresholdMinutes,
            Integer evaluatedOpenOrderCount,
            Integer staleOrderCount,
            Integer canceledCount,
            List<Item> items,
            Instant executedAt
    ) {
    }
}
