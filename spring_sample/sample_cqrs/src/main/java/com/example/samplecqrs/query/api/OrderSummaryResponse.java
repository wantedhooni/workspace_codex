package com.example.samplecqrs.query.api;

import com.example.samplecqrs.command.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record OrderSummaryResponse(
        String orderId,
        String customerId,
        String productCode,
        int quantity,
        BigDecimal totalAmount,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
