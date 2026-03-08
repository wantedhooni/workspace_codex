package com.example.samplesaga.order.api;

import com.example.samplesaga.order.domain.SagaOrderStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record SagaOrderResponse(
        String orderId,
        String customerId,
        String productCode,
        int quantity,
        BigDecimal totalAmount,
        SagaOrderStatus status,
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {
}
