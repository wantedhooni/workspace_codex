package com.example.sampleaibot.chat.websocket;

public record ChatSocketRequest(
    String type,
    String sessionId,
    String message
) {
}
