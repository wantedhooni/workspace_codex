package com.example.sampleaibot.chat.api;

import com.example.sampleaibot.chat.domain.ConversationMessage;
import com.example.sampleaibot.chat.service.OllamaChatService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final OllamaChatService ollamaChatService;

    public ChatController(OllamaChatService ollamaChatService) {
        this.ollamaChatService = ollamaChatService;
    }

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return ollamaChatService.chat(request);
    }

    @GetMapping("/session/{sessionId}")
    public ChatHistoryResponse history(@PathVariable String sessionId) {
        List<ConversationMessage> messages = ollamaChatService.history(sessionId);
        return new ChatHistoryResponse(sessionId, messages);
    }

    @DeleteMapping("/session/{sessionId}")
    public Map<String, Object> clear(@PathVariable String sessionId) {
        boolean cleared = ollamaChatService.clear(sessionId);
        return Map.of(
            "sessionId", sessionId,
            "cleared", cleared
        );
    }
}
