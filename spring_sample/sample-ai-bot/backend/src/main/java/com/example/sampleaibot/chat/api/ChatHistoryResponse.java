package com.example.sampleaibot.chat.api;

import com.example.sampleaibot.chat.domain.ConversationMessage;
import java.util.List;

public record ChatHistoryResponse(
    String sessionId,
    List<ConversationMessage> messages
) {
}
