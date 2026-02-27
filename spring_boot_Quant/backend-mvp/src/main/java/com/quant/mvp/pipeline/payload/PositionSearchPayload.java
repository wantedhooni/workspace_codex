package com.quant.mvp.pipeline.payload;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public final class PositionSearchPayload {

    private PositionSearchPayload() {
    }

    public record Req(
            @NotNull Long portfolioId
    ) {
    }

    public record Item(
            Long portfolioId,
            String symbol,
            BigDecimal quantity,
            BigDecimal avgPrice,
            BigDecimal lastPrice,
            BigDecimal marketValue,
            BigDecimal unrealizedPnl,
            BigDecimal realizedPnl
    ) {
    }

    public record Res(
            List<Item> items
    ) {
    }
}
