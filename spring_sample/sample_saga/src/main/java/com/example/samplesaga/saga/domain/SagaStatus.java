package com.example.samplesaga.saga.domain;

public enum SagaStatus {
    STARTED,
    PAYMENT_COMPLETED,
    INVENTORY_RESERVED,
    COMPLETED,
    FAILED,
    COMPENSATED
}
