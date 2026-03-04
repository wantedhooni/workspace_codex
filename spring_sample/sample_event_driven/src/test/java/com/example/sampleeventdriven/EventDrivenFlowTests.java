package com.example.sampleeventdriven;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.sampleeventdriven.event.OrderCreatedEvent;
import com.example.sampleeventdriven.order.api.PlaceOrderRequest;
import com.example.sampleeventdriven.order.application.OrderCommandService;
import com.example.sampleeventdriven.order.application.OrderProjectionService;
import com.example.sampleeventdriven.order.domain.CustomerOrderRepository;
import com.example.sampleeventdriven.order.domain.OrderStatus;
import com.example.sampleeventdriven.outbox.application.OutboxPublisherService;
import com.example.sampleeventdriven.outbox.domain.OutboxEventRepository;
import com.example.sampleeventdriven.payment.application.PaymentDecisionService;
import com.example.sampleeventdriven.payment.domain.PaymentRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class EventDrivenFlowTests {

    @Autowired
    private OrderCommandService orderCommandService;

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private OutboxPublisherService outboxPublisherService;

    @Autowired
    private PaymentDecisionService paymentDecisionService;

    @Autowired
    private PaymentRecordRepository paymentRecordRepository;

    @Autowired
    private OrderProjectionService orderProjectionService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        paymentRecordRepository.deleteAll();
        outboxEventRepository.deleteAll();
        customerOrderRepository.deleteAll();
    }

    @Test
    void orderCreationCreatesOutboxAndPaymentCanBeProjected() throws Exception {
        var order = orderCommandService.placeOrder(new PlaceOrderRequest("ACC-100", new BigDecimal("1200.50"), "idem-100"));

        assertThat(customerOrderRepository.findById(order.getId())).isPresent();
        assertThat(outboxEventRepository.countByPublishedFalse()).isEqualTo(1L);

        int publishedCount = outboxPublisherService.publishPendingEvents();
        assertThat(publishedCount).isEqualTo(1);

        var outbox = outboxEventRepository.findAll().getFirst();
        OrderCreatedEvent orderCreatedEvent = objectMapper.readValue(outbox.getPayload(), OrderCreatedEvent.class);

        var paymentResult = paymentDecisionService.decide(orderCreatedEvent);
        orderProjectionService.apply(paymentResult);

        var savedOrder = customerOrderRepository.findById(order.getId()).orElseThrow();
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(paymentRecordRepository.findByOrderId(order.getId())).isPresent();
    }

    @Test
    void idempotencyKeyPreventsDuplicateOrders() {
        var first = orderCommandService.placeOrder(new PlaceOrderRequest("ACC-200", new BigDecimal("300.00"), "idem-200"));
        var second = orderCommandService.placeOrder(new PlaceOrderRequest("ACC-200", new BigDecimal("300.00"), "idem-200"));

        assertThat(first.getId()).isEqualTo(second.getId());
        assertThat(customerOrderRepository.count()).isEqualTo(1L);
    }
}
