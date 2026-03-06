package com.example.sampleaibot.chat.api;

import java.time.Instant;

public record ChatResponse(
    String sessionId,
    String model,
    String answer,
    Instant respondedAt,
    int historySize
) {
}
