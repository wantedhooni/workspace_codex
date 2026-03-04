package com.example.sampleeventdriven.order.api;

import com.example.sampleeventdriven.order.domain.CustomerOrder;
import com.example.sampleeventdriven.order.domain.OrderStatus;
import java.math.BigDecimal;

public record OrderResponse(
        Long orderId,
        String accountId,
        BigDecimal amount,
        String idempotencyKey,
        OrderStatus status
) {
    public static OrderResponse from(CustomerOrder order) {
        return new OrderResponse(order.getId(), order.getAccountId(), order.getAmount(), order.getIdempotencyKey(), order.getStatus());
    }
}
