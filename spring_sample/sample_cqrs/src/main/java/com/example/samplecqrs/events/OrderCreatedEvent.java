package com.example.samplecqrs.events;

import com.example.samplecqrs.command.domain.OrderStatus;
import com.example.samplecqrs.command.domain.PurchaseOrder;
import java.math.BigDecimal;
import java.time.Instant;

public record OrderCreatedEvent(
        String orderId,
        String customerId,
        String productCode,
        int quantity,
        BigDecimal totalAmount,
        OrderStatus status,
        Instant occurredAt
) {

    public static OrderCreatedEvent from(PurchaseOrder order) {
        return new OrderCreatedEvent(
                order.getId(),
                order.getCustomerId(),
                order.getProductCode(),
                order.getQuantity(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}
