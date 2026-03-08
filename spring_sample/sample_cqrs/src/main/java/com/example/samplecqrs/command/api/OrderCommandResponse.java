package com.example.samplecqrs.command.api;

import com.example.samplecqrs.command.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record OrderCommandResponse(
        String orderId,
        String customerId,
        String productCode,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
