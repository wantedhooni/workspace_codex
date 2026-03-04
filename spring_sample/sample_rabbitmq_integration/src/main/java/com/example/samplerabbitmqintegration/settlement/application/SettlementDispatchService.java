package com.example.samplerabbitmqintegration.settlement.application;

import com.example.samplerabbitmqintegration.config.RabbitMessagingProperties;
import com.example.samplerabbitmqintegration.settlement.api.DispatchSettlementRequest;
import com.example.samplerabbitmqintegration.settlement.api.SettlementStatusResponse;
import com.example.samplerabbitmqintegration.settlement.domain.SettlementMessage;
import java.time.Instant;
import java.util.NoSuchElementException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class SettlementDispatchService {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMessagingProperties properties;
    private final SettlementStatusStore statusStore;

    public SettlementDispatchService(
            RabbitTemplate rabbitTemplate,
            RabbitMessagingProperties properties,
            SettlementStatusStore statusStore
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
        this.statusStore = statusStore;
    }

    public SettlementStatusResponse dispatch(DispatchSettlementRequest request) {
        SettlementMessage message = new SettlementMessage(
                request.settlementId(),
                request.bookCode(),
                request.currency(),
                request.amount(),
                request.counterparty(),
                Instant.now()
        );
        statusStore.markPublished(message);
        rabbitTemplate.convertAndSend(properties.exchange(), properties.routingKey(), message);
        return statusStore.get(request.settlementId());
    }

    public SettlementStatusResponse getStatus(String settlementId) {
        SettlementStatusResponse response = statusStore.get(settlementId);
        if (response == null) {
            throw new NoSuchElementException("settlementId=%s 에 대한 처리 상태가 없다.".formatted(settlementId));
        }
        return response;
    }
}
