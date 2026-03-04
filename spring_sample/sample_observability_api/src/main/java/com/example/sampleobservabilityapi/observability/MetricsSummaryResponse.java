package com.example.sampleobservabilityapi.observability;

public record MetricsSummaryResponse(
        double processedCount,
        double failedCount,
        double averageDurationMs,
        String lastOutcome
) {
}
