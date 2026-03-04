package com.example.webfluxsample.operations;

public record LatencyCheckResponse(
        String domain,
        int measuredLatencyMillis,
        int thresholdMillis,
        boolean withinTarget
) {
}
