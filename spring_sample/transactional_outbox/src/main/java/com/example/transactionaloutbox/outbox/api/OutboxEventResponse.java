package com.example.transactionaloutbox.outbox.api;

import com.example.transactionaloutbox.outbox.domain.OutboxStatus;
import java.time.Instant;

public record OutboxEventResponse(
        String outboxEventId,
        String aggregateType,
        String aggregateId,
        String eventType,
        String topic,
        OutboxStatus status,
        int attemptCount,
        Instant nextAttemptAt,
        Instant publishedAt,
        String lastErrorMessage,
        Instant createdAt,
        Instant updatedAt
) {
}
