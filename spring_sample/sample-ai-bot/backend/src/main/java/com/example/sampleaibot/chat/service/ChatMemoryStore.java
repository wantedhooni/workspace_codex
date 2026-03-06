package com.example.sampleaibot.chat.service;

import com.example.sampleaibot.chat.config.OllamaProperties;
import com.example.sampleaibot.chat.domain.ConversationMessage;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class ChatMemoryStore {

    private final Map<String, Deque<ConversationMessage>> sessions = new ConcurrentHashMap<>();
    private final int maxStoredMessages;

    public ChatMemoryStore(OllamaProperties ollamaProperties) {
        this.maxStoredMessages = ollamaProperties.maxHistory() * 2;
    }

    public List<ConversationMessage> findBySessionId(String sessionId) {
        Deque<ConversationMessage> messages = sessions.get(sessionId);
        if (messages == null) {
            return List.of();
        }
        synchronized (messages) {
            return List.copyOf(messages);
        }
    }

    public void append(String sessionId, ConversationMessage userMessage, ConversationMessage assistantMessage) {
        Deque<ConversationMessage> messages = sessions.computeIfAbsent(sessionId, key -> new ArrayDeque<>());
        synchronized (messages) {
            messages.addLast(userMessage);
            messages.addLast(assistantMessage);
            while (messages.size() > maxStoredMessages) {
                messages.removeFirst();
            }
        }
    }

    public int size(String sessionId) {
        Deque<ConversationMessage> messages = sessions.get(sessionId);
        if (messages == null) {
            return 0;
        }
        synchronized (messages) {
            return messages.size();
        }
    }

    public boolean clear(String sessionId) {
        return sessions.remove(sessionId) != null;
    }
}
