package com.example.transactionaloutbox.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedEventListener.class);

    private final ObjectMapper objectMapper;

    public OrderCreatedEventListener(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.order-created-topic}", groupId = "transactional-outbox-audit")
    public void consume(String payload) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);
            log.info("주문 생성 이벤트 수신. orderId={}, productId={}, quantity={}",
                    event.orderId(), event.productId(), event.quantity());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Kafka 이벤트 역직렬화에 실패했습니다.", exception);
        }
    }
}
