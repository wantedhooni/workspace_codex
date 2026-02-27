package com.quant.mvp.pipeline.payload;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class ExecutionQualityPayload {

    private ExecutionQualityPayload() {
    }

    public record Item(
            String qualityKey,
            Long portfolioId,
            String symbol,
            Integer orderCount,
            Integer filledOrderCount,
            BigDecimal fillRatePct,
            Integer tradeCount,
            BigDecimal executedQuantity,
            BigDecimal executedNotional,
            BigDecimal averageFillPrice,
            BigDecimal averageFeeBps,
            BigDecimal averageSlippageBps,
            BigDecimal netCashFlow,
            String qualityGrade,
            String qualityNote,
            Instant lastTradedAt
    ) {
    }

    public record Res(
            List<Item> items
    ) {
    }
}
