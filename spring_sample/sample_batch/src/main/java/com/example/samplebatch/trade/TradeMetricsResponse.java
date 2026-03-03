package com.example.samplebatch.trade;

public record TradeMetricsResponse(
        long rawTotal,
        long rawPending,
        long rawProcessed,
        long summaryTotal
) {
}
