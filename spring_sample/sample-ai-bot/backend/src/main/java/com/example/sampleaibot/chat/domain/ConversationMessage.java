package com.example.sampleaibot.chat.domain;

import java.time.Instant;

public record ConversationMessage(
    String role,
    String content,
    Instant createdAt
) {
}
