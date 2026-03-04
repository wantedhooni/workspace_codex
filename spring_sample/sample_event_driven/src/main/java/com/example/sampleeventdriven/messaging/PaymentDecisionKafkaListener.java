package com.example.sampleeventdriven.messaging;

import com.example.sampleeventdriven.event.OrderCreatedEvent;
import com.example.sampleeventdriven.payment.application.PaymentDecisionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class PaymentDecisionKafkaListener {

    private final PaymentDecisionService paymentDecisionService;
    private final ObjectMapper objectMapper;

    public PaymentDecisionKafkaListener(PaymentDecisionService paymentDecisionService, ObjectMapper objectMapper) {
        this.paymentDecisionService = paymentDecisionService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.events.orders-topic}", groupId = "sample-event-driven-payment")
    public void onMessage(String payload) throws Exception {
        paymentDecisionService.handle(objectMapper.readValue(payload, OrderCreatedEvent.class));
    }
}
