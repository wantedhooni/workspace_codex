package com.example.sampleaibot.chat.service;

import com.example.sampleaibot.chat.config.OllamaProperties;
import com.example.sampleaibot.chat.domain.ConversationMessage;
import java.time.Instant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ChatMemoryStoreTests {

    @Test
    void shouldKeepOnlyConfiguredHistoryWindow() {
        OllamaProperties properties = new OllamaProperties(
            "http://localhost:11434",
            "llama3.2:1b",
            "test prompt",
            2,
            0.4,
            128,
            "30m",
            120
        );
        ChatMemoryStore store = new ChatMemoryStore(properties);

        store.append("s1", user("u1"), assistant("a1"));
        store.append("s1", user("u2"), assistant("a2"));
        store.append("s1", user("u3"), assistant("a3"));

        Assertions.assertEquals(4, store.size("s1"));
        Assertions.assertEquals("u2", store.findBySessionId("s1").getFirst().content());
    }

    private ConversationMessage user(String content) {
        return new ConversationMessage("user", content, Instant.now());
    }

    private ConversationMessage assistant(String content) {
        return new ConversationMessage("assistant", content, Instant.now());
    }
}
