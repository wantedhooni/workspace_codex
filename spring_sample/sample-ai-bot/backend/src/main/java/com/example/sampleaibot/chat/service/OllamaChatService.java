package com.example.sampleaibot.chat.service;

import com.example.sampleaibot.chat.api.ChatRequest;
import com.example.sampleaibot.chat.api.ChatResponse;
import com.example.sampleaibot.chat.config.OllamaProperties;
import com.example.sampleaibot.chat.domain.ConversationMessage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class OllamaChatService {

    private final RestClient restClient;
    private final OllamaProperties ollamaProperties;
    private final ChatMemoryStore chatMemoryStore;

    public OllamaChatService(RestClient.Builder restClientBuilder,
                             OllamaProperties ollamaProperties,
                             ChatMemoryStore chatMemoryStore) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(ollamaProperties.requestTimeoutSeconds() * 1000);

        this.restClient = restClientBuilder
            .baseUrl(ollamaProperties.baseUrl())
            .requestFactory(requestFactory)
            .build();
        this.ollamaProperties = ollamaProperties;
        this.chatMemoryStore = chatMemoryStore;
    }

    public ChatResponse chat(ChatRequest request) {
        String sessionId = resolveSessionId(request.sessionId());
        String model = resolveModel(request.model());

        List<OllamaMessagePayload> messages = buildMessages(sessionId, request.message());
        OllamaChatRequest payload = new OllamaChatRequest(
            model,
            messages,
            false,
            ollamaProperties.keepAlive(),
            new OllamaOptions(ollamaProperties.temperature(), ollamaProperties.numPredict())
        );

        OllamaChatResult result = callOllama(payload);
        String answer = extractAnswer(result);

        chatMemoryStore.append(
            sessionId,
            new ConversationMessage("user", request.message(), Instant.now()),
            new ConversationMessage("assistant", answer, Instant.now())
        );

        return new ChatResponse(
            sessionId,
            model,
            answer,
            Instant.now(),
            chatMemoryStore.size(sessionId)
        );
    }

    public List<ConversationMessage> history(String sessionId) {
        return chatMemoryStore.findBySessionId(sessionId);
    }

    public boolean clear(String sessionId) {
        return chatMemoryStore.clear(sessionId);
    }

    private String resolveSessionId(String sessionId) {
        if (StringUtils.hasText(sessionId)) {
            return sessionId.trim();
        }
        return UUID.randomUUID().toString();
    }

    private String resolveModel(String model) {
        if (StringUtils.hasText(model)) {
            return model.trim();
        }
        return ollamaProperties.model();
    }

    private List<OllamaMessagePayload> buildMessages(String sessionId, String userMessage) {
        List<OllamaMessagePayload> messages = new ArrayList<>();
        messages.add(new OllamaMessagePayload("system", ollamaProperties.systemPrompt()));

        for (ConversationMessage message : chatMemoryStore.findBySessionId(sessionId)) {
            messages.add(new OllamaMessagePayload(message.role(), message.content()));
        }
        messages.add(new OllamaMessagePayload("user", userMessage));

        return messages;
    }

    private OllamaChatResult callOllama(OllamaChatRequest payload) {
        try {
            OllamaChatResult response = restClient.post()
                .uri("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(OllamaChatResult.class);

            if (response == null) {
                throw new OllamaChatException("Ollama 응답이 비어 있습니다.");
            }
            return response;
        } catch (RestClientException exception) {
            throw new OllamaChatException("Ollama 호출에 실패했습니다. ollama 서버와 모델 상태를 확인하세요.", exception);
        }
    }

    private String extractAnswer(OllamaChatResult result) {
        if (result.message() == null || !StringUtils.hasText(result.message().content())) {
            throw new OllamaChatException("Ollama 응답에서 답변 본문을 찾을 수 없습니다.");
        }
        return result.message().content();
    }

    private record OllamaChatRequest(
        String model,
        List<OllamaMessagePayload> messages,
        boolean stream,
        String keep_alive,
        OllamaOptions options
    ) {
    }

    private record OllamaMessagePayload(
        String role,
        String content
    ) {
    }

    private record OllamaOptions(
        double temperature,
        int num_predict
    ) {
    }

    private record OllamaChatResult(
        String model,
        String created_at,
        OllamaMessagePayload message,
        boolean done
    ) {
    }
}
