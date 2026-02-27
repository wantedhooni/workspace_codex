package com.quant.mvp.pipeline.payload;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderHealthPayload {

    private OrderHealthPayload() {
    }

    public record Item(
            String healthKey,
            Long portfolioId,
            String symbol,
            Integer openOrderCount,
            Integer staleOrderCount,
            Integer staleThresholdMinutes,
            BigDecimal openOrderUsagePct,
            BigDecimal averageOpenAgeMinutes,
            Long maxOpenAgeMinutes,
            Long oldestOpenOrderId,
            String healthStatus,
            String healthNote,
            Instant updatedAt
    ) {
    }

    public record Res(
            List<Item> items
    ) {
    }
}
