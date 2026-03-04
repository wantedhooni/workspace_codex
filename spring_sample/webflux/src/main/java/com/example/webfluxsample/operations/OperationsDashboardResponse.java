package com.example.webfluxsample.operations;

public record OperationsDashboardResponse(
        String domain,
        int requestsPerSecond,
        double errorRate,
        int p95LatencyMillis,
        String status
) {
}
