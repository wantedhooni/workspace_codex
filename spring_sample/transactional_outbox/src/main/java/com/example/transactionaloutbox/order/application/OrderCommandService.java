package com.example.transactionaloutbox.order.application;

import com.example.transactionaloutbox.config.KafkaTopicsProperties;
import com.example.transactionaloutbox.messaging.OrderCreatedEvent;
import com.example.transactionaloutbox.order.api.CreateOrderRequest;
import com.example.transactionaloutbox.order.api.OrderResponse;
import com.example.transactionaloutbox.order.domain.PurchaseOrder;
import com.example.transactionaloutbox.order.domain.PurchaseOrderRepository;
import com.example.transactionaloutbox.outbox.domain.OutboxEvent;
import com.example.transactionaloutbox.outbox.domain.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderCommandService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTopicsProperties kafkaTopicsProperties;

    public OrderCommandService(
            PurchaseOrderRepository purchaseOrderRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper,
            KafkaTopicsProperties kafkaTopicsProperties
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.kafkaTopicsProperties = kafkaTopicsProperties;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        PurchaseOrder order = purchaseOrderRepository.save(
                new PurchaseOrder(request.customerId(), request.productId(), request.quantity(), request.unitPrice())
        );

        OrderCreatedEvent event = OrderCreatedEvent.from(order);
        outboxEventRepository.save(
                OutboxEvent.pending(
                        "PurchaseOrder",
                        order.getId(),
                        "OrderCreated",
                        kafkaTopicsProperties.orderCreatedTopic(),
                        serialize(event)
                )
        );

        return OrderResponseMapper.toResponse(order);
    }

    private String serialize(OrderCreatedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("주문 생성 이벤트 직렬화에 실패했습니다.", exception);
        }
    }
}
