package com.example.samplesaga.saga.domain;

public enum SagaStepStatus {
    PENDING,
    COMPLETED,
    COMPENSATED,
    FAILED,
    SKIPPED
}
