package com.commerce.service_order.events;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class OrderEventPublisher {

    private static final String TOPIC = "order.events";
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public OrderEventPublisher(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void orderCreated(Long orderId, Long customerId, Integer totalAmount, String status) {
        publish("ORDER_CREATED", orderId, customerId, totalAmount, status);
    }

    public void statusChanged(Long orderId, Long customerId, Integer totalAmount, String status) {
        publish("ORDER_STATUS_CHANGED", orderId, customerId, totalAmount, status);
    }

    private void publish(String type, Long orderId, Long customerId, Integer totalAmount, String status) {
        OrderEvent event = new OrderEvent(
            UUID.randomUUID().toString(),
            type,
            orderId,
            customerId,
            totalAmount,
            status,
            Instant.now()
        );
        kafkaTemplate.send(TOPIC, String.valueOf(orderId), event);
    }
}
