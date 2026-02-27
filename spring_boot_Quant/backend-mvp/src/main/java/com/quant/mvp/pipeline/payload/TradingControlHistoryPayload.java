package com.quant.mvp.pipeline.payload;

import java.time.Instant;
import java.util.List;

public final class TradingControlHistoryPayload {

    private TradingControlHistoryPayload() {
    }

    public record Req(
            Long portfolioId,
            Integer limit
    ) {
    }

    public record Item(
            Long historyId,
            Long portfolioId,
            Boolean previousTradingEnabled,
            Boolean tradingEnabled,
            String action,
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
