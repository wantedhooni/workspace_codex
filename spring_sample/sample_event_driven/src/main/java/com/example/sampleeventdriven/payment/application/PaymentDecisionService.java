package com.example.sampleeventdriven.payment.application;

import com.example.sampleeventdriven.config.EventFlowProperties;
import com.example.sampleeventdriven.event.OrderCreatedEvent;
import com.example.sampleeventdriven.event.PaymentResultEvent;
import com.example.sampleeventdriven.messaging.DomainEventPublisher;
import com.example.sampleeventdriven.payment.domain.PaymentRecord;
import com.example.sampleeventdriven.payment.domain.PaymentRecordRepository;
import com.example.sampleeventdriven.payment.domain.PaymentStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentDecisionService {

    private final PaymentRecordRepository paymentRecordRepository;
    private final DomainEventPublisher domainEventPublisher;
    private final EventFlowProperties eventFlowProperties;
    private final ObjectMapper objectMapper;

    public PaymentDecisionService(
            PaymentRecordRepository paymentRecordRepository,
            DomainEventPublisher domainEventPublisher,
            EventFlowProperties eventFlowProperties,
            ObjectMapper objectMapper
    ) {
        this.paymentRecordRepository = paymentRecordRepository;
        this.domainEventPublisher = domainEventPublisher;
        this.eventFlowProperties = eventFlowProperties;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public PaymentResultEvent decide(OrderCreatedEvent event) {
        boolean authorized = event.amount().compareTo(new BigDecimal("50000.00")) <= 0;
        PaymentStatus status = authorized ? PaymentStatus.AUTHORIZED : PaymentStatus.DECLINED;

        paymentRecordRepository.findByOrderId(event.orderId())
                .orElseGet(() -> paymentRecordRepository.save(new PaymentRecord(event.orderId(), event.amount(), status)));

        return new PaymentResultEvent(
                event.orderId(),
                event.accountId(),
                event.amount(),
                status.name()
        );
    }

    public void handle(OrderCreatedEvent event) {
        PaymentResultEvent result = decide(event);
        domainEventPublisher.publish(eventFlowProperties.getPaymentsTopic(), String.valueOf(result.orderId()), writePayload(result));
    }

    private String writePayload(PaymentResultEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("결제 결과 이벤트 직렬화에 실패했습니다.", exception);
        }
    }
}
