package com.example.sampleaibot.chat.api;

import java.time.Instant;

public record ApiErrorResponse(
    String code,
    String message,
    Instant timestamp
) {
}
