package com.example.samplekafkaintegration.messaging.application;

import com.example.samplekafkaintegration.messaging.api.TradeInstructionStatusResponse;
import com.example.samplekafkaintegration.messaging.domain.TradeInstructionMessage;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class TradeInstructionStatusStore {

    private final Map<String, TradeInstructionStatusResponse> statuses = new ConcurrentHashMap<>();

    public void markPublished(TradeInstructionMessage message) {
        statuses.put(message.messageId(), toResponse(message, "PUBLISHED", "Kafka 토픽 발행 완료"));
    }

    public void markProcessed(TradeInstructionMessage message) {
        statuses.put(message.messageId(), toResponse(message, "PROCESSED", "거래 지시 처리 완료"));
    }

    public void markFailed(TradeInstructionMessage message, String detail) {
        statuses.put(message.messageId(), toResponse(message, "FAILED", detail));
    }

    public TradeInstructionStatusResponse get(String messageId) {
        return statuses.get(messageId);
    }

    private TradeInstructionStatusResponse toResponse(TradeInstructionMessage message, String status, String detail) {
        return new TradeInstructionStatusResponse(
                message.messageId(),
                message.accountId(),
                message.instrumentCode(),
                message.quantity(),
                message.counterparty(),
                status,
                detail,
                Instant.now()
        );
    }
}
