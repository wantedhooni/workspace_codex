package com.example.sampleaibot.chat.websocket;

import com.example.sampleaibot.chat.api.ChatRequest;
import com.example.sampleaibot.chat.api.ChatResponse;
import com.example.sampleaibot.chat.service.OllamaChatException;
import com.example.sampleaibot.chat.service.OllamaChatService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final OllamaChatService ollamaChatService;
    private final Map<String, String> socketToChatSession = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(ObjectMapper objectMapper, OllamaChatService ollamaChatService) {
        this.objectMapper = objectMapper;
        this.ollamaChatService = ollamaChatService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        send(session, new ChatSocketResponse(
            "connected",
            socketToChatSession.get(session.getId()),
            null,
            null,
            null,
            null,
            null
        ));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            ChatSocketRequest request = parseRequest(message.getPayload());
            String type = resolveType(request.type());

            switch (type) {
                case "chat" -> handleChat(session, request);
                case "clear" -> handleClear(session, request);
                case "ping" -> send(session, new ChatSocketResponse("pong", null, null, null, null, null, null));
                default -> sendError(session, "지원하지 않는 메시지 타입입니다: " + type);
            }
        } catch (JsonProcessingException exception) {
            sendError(session, "요청 본문이 JSON 형식이 아닙니다.");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        socketToChatSession.remove(session.getId());
    }

    private void handleChat(WebSocketSession socketSession, ChatSocketRequest request) throws IOException {
        if (!StringUtils.hasText(request.message())) {
            sendError(socketSession, "message는 필수입니다.");
            return;
        }

        String sessionId = resolveChatSessionId(socketSession, request.sessionId());

        try {
            ChatResponse response = ollamaChatService.chat(new ChatRequest(sessionId, request.message(), null));
            socketToChatSession.put(socketSession.getId(), response.sessionId());

            send(socketSession, new ChatSocketResponse(
                "chat.response",
                response.sessionId(),
                response.answer(),
                response.respondedAt(),
                response.historySize(),
                null,
                null
            ));
        } catch (OllamaChatException exception) {
            sendError(socketSession, exception.getMessage());
        }
    }

    private void handleClear(WebSocketSession socketSession, ChatSocketRequest request) throws IOException {
        String sessionId = resolveChatSessionId(socketSession, request.sessionId());
        if (!StringUtils.hasText(sessionId)) {
            sendError(socketSession, "초기화할 sessionId가 없습니다.");
            return;
        }

        boolean cleared = ollamaChatService.clear(sessionId);
        socketToChatSession.remove(socketSession.getId());
        send(socketSession, new ChatSocketResponse(
            "chat.cleared",
            sessionId,
            null,
            null,
            null,
            cleared,
            null
        ));
    }

    private ChatSocketRequest parseRequest(String payload) throws JsonProcessingException {
        return objectMapper.readValue(payload, ChatSocketRequest.class);
    }

    private String resolveType(String type) {
        if (!StringUtils.hasText(type)) {
            return "chat";
        }
        return type.trim();
    }

    private String resolveChatSessionId(WebSocketSession socketSession, String requestedSessionId) {
        if (StringUtils.hasText(requestedSessionId)) {
            return requestedSessionId.trim();
        }
        String boundSessionId = socketToChatSession.get(socketSession.getId());
        if (StringUtils.hasText(boundSessionId)) {
            return boundSessionId;
        }
        return null;
    }

    private void sendError(WebSocketSession session, String message) throws IOException {
        send(session, new ChatSocketResponse(
            "chat.error",
            socketToChatSession.get(session.getId()),
            null,
            null,
            null,
            null,
            message
        ));
    }

    private void send(WebSocketSession session, ChatSocketResponse response) throws IOException {
        if (!session.isOpen()) {
            return;
        }
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }
}
