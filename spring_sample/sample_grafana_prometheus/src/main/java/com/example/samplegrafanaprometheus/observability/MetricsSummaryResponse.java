package com.example.samplegrafanaprometheus.observability;

public record MetricsSummaryResponse(
        double processedTotal,
        double failedTotal,
        double averageLatencyMs
) {
}
