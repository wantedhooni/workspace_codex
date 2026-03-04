package com.example.samplerabbitmqintegration.settlement.infrastructure;

import com.example.samplerabbitmqintegration.config.RabbitMessagingProperties;
import com.example.samplerabbitmqintegration.settlement.application.SettlementStatusStore;
import com.example.samplerabbitmqintegration.settlement.domain.SettlementMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SettlementRabbitListener {

    private final SettlementStatusStore statusStore;

    public SettlementRabbitListener(SettlementStatusStore statusStore) {
        this.statusStore = statusStore;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void consume(SettlementMessage message) {
        if ("FAIL".equalsIgnoreCase(message.counterparty())) {
            throw new IllegalStateException("의도된 소비 실패");
        }
        statusStore.markProcessed(message);
    }

    @RabbitListener(queues = "${app.rabbitmq.dlq}")
    public void consumeDlq(SettlementMessage message) {
        statusStore.markFailed(message, "처리 실패로 DLQ 적재");
    }
}
