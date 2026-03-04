package com.example.samplekafkaintegration.messaging.application;

import com.example.samplekafkaintegration.config.KafkaIntegrationProperties;
import com.example.samplekafkaintegration.messaging.api.TradeInstructionRequest;
import com.example.samplekafkaintegration.messaging.api.TradeInstructionStatusResponse;
import com.example.samplekafkaintegration.messaging.domain.TradeInstructionMessage;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TradeInstructionService {

    private final KafkaTemplate<String, TradeInstructionMessage> kafkaTemplate;
    private final KafkaIntegrationProperties properties;
    private final TradeInstructionStatusStore statusStore;

    public TradeInstructionService(
            KafkaTemplate<String, TradeInstructionMessage> kafkaTemplate,
            KafkaIntegrationProperties properties,
            TradeInstructionStatusStore statusStore
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
        this.statusStore = statusStore;
    }

    public TradeInstructionStatusResponse publish(TradeInstructionRequest request) {
        String messageId = UUID.randomUUID().toString();
        TradeInstructionMessage message = new TradeInstructionMessage(
                messageId,
                request.accountId(),
                request.instrumentCode(),
                request.quantity(),
                request.counterparty(),
                Instant.now()
        );
        statusStore.markPublished(message);
        kafkaTemplate.send(properties.tradeTopic(), message.messageId(), message);
        return statusStore.get(messageId);
    }

    public TradeInstructionStatusResponse getStatus(String messageId) {
        TradeInstructionStatusResponse response = statusStore.get(messageId);
        if (response == null) {
            throw new NoSuchElementException("messageId=%s 에 대한 처리 상태가 없다.".formatted(messageId));
        }
        return response;
    }
}
