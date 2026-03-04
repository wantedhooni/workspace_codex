package com.example.sampleeventdriven.order.application;

import com.example.sampleeventdriven.config.EventFlowProperties;
import com.example.sampleeventdriven.event.OrderCreatedEvent;
import com.example.sampleeventdriven.order.api.PlaceOrderRequest;
import com.example.sampleeventdriven.order.domain.CustomerOrder;
import com.example.sampleeventdriven.order.domain.CustomerOrderRepository;
import com.example.sampleeventdriven.order.domain.OrderStatus;
import com.example.sampleeventdriven.outbox.domain.OutboxEvent;
import com.example.sampleeventdriven.outbox.domain.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderCommandService {

    private final CustomerOrderRepository customerOrderRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final EventFlowProperties eventFlowProperties;
    private final ObjectMapper objectMapper;

    public OrderCommandService(
            CustomerOrderRepository customerOrderRepository,
            OutboxEventRepository outboxEventRepository,
            EventFlowProperties eventFlowProperties,
            ObjectMapper objectMapper
    ) {
        this.customerOrderRepository = customerOrderRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.eventFlowProperties = eventFlowProperties;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CustomerOrder placeOrder(PlaceOrderRequest request) {
        return customerOrderRepository.findByIdempotencyKey(request.idempotencyKey())
                .orElseGet(() -> createNewOrder(request));
    }

    private CustomerOrder createNewOrder(PlaceOrderRequest request) {
        CustomerOrder order = customerOrderRepository.save(new CustomerOrder(request.accountId(), request.amount(), request.idempotencyKey()));
        order.markPaymentPending();
        customerOrderRepository.save(order);

        String payload = writePayload(new OrderCreatedEvent(order.getId(), order.getAccountId(), order.getAmount()));
        outboxEventRepository.save(new OutboxEvent(
                "ORDER",
                String.valueOf(order.getId()),
                OrderCreatedEvent.class.getSimpleName(),
                eventFlowProperties.getOrdersTopic(),
                payload
        ));

        return order;
    }

    private String writePayload(OrderCreatedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("주문 이벤트 직렬화에 실패했습니다.", exception);
        }
    }
}
