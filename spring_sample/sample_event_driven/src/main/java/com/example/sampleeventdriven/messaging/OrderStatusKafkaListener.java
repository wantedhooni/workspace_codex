package com.example.sampleeventdriven.messaging;

import com.example.sampleeventdriven.event.PaymentResultEvent;
import com.example.sampleeventdriven.order.application.OrderProjectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class OrderStatusKafkaListener {

    private final OrderProjectionService orderProjectionService;
    private final ObjectMapper objectMapper;

    public OrderStatusKafkaListener(OrderProjectionService orderProjectionService, ObjectMapper objectMapper) {
        this.orderProjectionService = orderProjectionService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.events.payments-topic}", groupId = "sample-event-driven-order")
    public void onMessage(String payload) throws Exception {
        orderProjectionService.apply(objectMapper.readValue(payload, PaymentResultEvent.class));
    }
}
