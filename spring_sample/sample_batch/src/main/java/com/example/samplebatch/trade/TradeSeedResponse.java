package com.example.samplebatch.trade;

import java.time.Duration;

public record TradeSeedResponse(
        int requestedSize,
        int insertedRows,
        int batchSize,
        Duration elapsed,
        TradeMetricsResponse metrics
) {
}
