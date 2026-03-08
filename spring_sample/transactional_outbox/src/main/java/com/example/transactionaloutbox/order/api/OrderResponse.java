package com.example.transactionaloutbox.order.api;

import com.example.transactionaloutbox.order.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        String orderId,
        String customerId,
        String productId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        OrderStatus status,
        Instant createdAt
) {
}
