package com.example.samplesaga.saga.api;

import com.example.samplesaga.saga.domain.SagaStatus;
import com.example.samplesaga.saga.domain.SagaStepStatus;
import java.time.Instant;

public record OrderSagaResponse(
        String sagaId,
        String orderId,
        SagaStatus status,
        SagaStepStatus paymentStatus,
        SagaStepStatus inventoryStatus,
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {
}
