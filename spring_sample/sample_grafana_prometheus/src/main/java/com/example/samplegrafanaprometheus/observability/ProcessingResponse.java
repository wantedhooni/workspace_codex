package com.example.samplegrafanaprometheus.observability;

public record ProcessingResponse(
        String channel,
        String workType,
        long volume,
        long durationMs,
        String status
) {
}
