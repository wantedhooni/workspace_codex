package com.example.transactionaloutbox.outbox.domain;

public enum OutboxStatus {
    PENDING,
    PROCESSING,
    PUBLISHED
}
