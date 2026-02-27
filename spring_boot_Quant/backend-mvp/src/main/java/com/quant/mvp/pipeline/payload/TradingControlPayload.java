package com.quant.mvp.pipeline.payload;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public final class TradingControlPayload {

    private TradingControlPayload() {
    }

    public record Req(
            @NotNull Long portfolioId,
            @NotNull Boolean tradingEnabled,
            @Size(max = 200) String reason
    ) {
    }

    public record Item(
            Long portfolioId,
            Boolean tradingEnabled,
            String reason,
            Instant updatedAt,
            String updatedBy
    ) {
    }

    public record Res(
            List<Item> items
    ) {
    }
}
