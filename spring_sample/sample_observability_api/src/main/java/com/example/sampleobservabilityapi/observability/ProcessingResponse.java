package com.example.sampleobservabilityapi.observability;

public record ProcessingResponse(
        String workloadId,
        String status,
        long durationMs
) {
}
