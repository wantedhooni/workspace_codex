package com.example.samplecqrs.events;

import com.example.samplecqrs.command.domain.OrderStatus;
import com.example.samplecqrs.command.domain.PurchaseOrder;
import java.time.Instant;

public record OrderCancelledEvent(
        String orderId,
        OrderStatus status,
        Instant occurredAt
) {

    public static OrderCancelledEvent from(PurchaseOrder order) {
        return new OrderCancelledEvent(order.getId(), order.getStatus(), Instant.now());
    }
}
