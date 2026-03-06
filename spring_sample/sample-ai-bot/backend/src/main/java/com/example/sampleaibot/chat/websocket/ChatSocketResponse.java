package com.example.sampleaibot.chat.websocket;

import java.time.Instant;

public record ChatSocketResponse(
    String type,
    String sessionId,
    String answer,
    Instant respondedAt,
    Integer historySize,
    Boolean cleared,
    String error
) {
}
