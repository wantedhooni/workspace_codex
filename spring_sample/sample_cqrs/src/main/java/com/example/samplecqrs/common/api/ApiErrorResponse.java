package com.example.samplecqrs.common.api;

import java.time.Instant;

public record ApiErrorResponse(
        String message,
        Instant timestamp
) {
}
