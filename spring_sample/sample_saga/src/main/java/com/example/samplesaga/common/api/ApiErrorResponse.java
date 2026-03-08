package com.example.samplesaga.common.api;

import java.time.Instant;

public record ApiErrorResponse(
        String message,
        Instant timestamp
) {
}
