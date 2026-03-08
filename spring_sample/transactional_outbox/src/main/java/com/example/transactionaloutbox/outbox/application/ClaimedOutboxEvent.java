package com.example.transactionaloutbox.outbox.application;

public record ClaimedOutboxEvent(
        String outboxEventId,
        String aggregateId,
        String topic,
        String payload
) {
}
