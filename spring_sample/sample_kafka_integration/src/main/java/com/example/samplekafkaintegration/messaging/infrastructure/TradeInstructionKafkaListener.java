package com.example.samplekafkaintegration.messaging.infrastructure;

import com.example.samplekafkaintegration.config.KafkaIntegrationProperties;
import com.example.samplekafkaintegration.messaging.application.TradeInstructionStatusStore;
import com.example.samplekafkaintegration.messaging.domain.TradeInstructionMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TradeInstructionKafkaListener {

    private final TradeInstructionStatusStore statusStore;

    public TradeInstructionKafkaListener(TradeInstructionStatusStore statusStore) {
        this.statusStore = statusStore;
    }

    @KafkaListener(
            topics = "${app.kafka.trade-topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "tradeKafkaListenerContainerFactory"
    )
    public void consume(TradeInstructionMessage message) {
        if ("FAIL".equalsIgnoreCase(message.counterparty())) {
            throw new IllegalStateException("의도된 소비 실패");
        }
        statusStore.markProcessed(message);
    }

    @KafkaListener(
            topics = "${app.kafka.trade-dlt-topic}",
            groupId = "${spring.kafka.consumer.group-id}.dlt",
            containerFactory = "tradeKafkaListenerContainerFactory"
    )
    public void consumeDeadLetter(TradeInstructionMessage message) {
        statusStore.markFailed(message, "재시도 초과로 DLT 적재");
    }
}
