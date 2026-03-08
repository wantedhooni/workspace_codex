package com.example.transactionaloutbox.outbox.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import java.time.Instant;
import java.util.UUID;

@Entity
public class OutboxEvent {

    @Id
    private String id;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private String aggregateId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String topic;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    @Column(nullable = false)
    private int attemptCount;

    @Column(nullable = false)
    private Instant nextAttemptAt;

    @Column
    private Instant publishedAt;

    @Column(length = 1000)
    private String lastErrorMessage;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected OutboxEvent() {
    }

    private OutboxEvent(String aggregateType, String aggregateId, String eventType, String topic, String payload) {
        Instant now = Instant.now();
        this.id = UUID.randomUUID().toString();
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.topic = topic;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.attemptCount = 0;
        this.nextAttemptAt = now;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static OutboxEvent pending(
            String aggregateType,
            String aggregateId,
            String eventType,
            String topic,
            String payload
    ) {
        return new OutboxEvent(aggregateType, aggregateId, eventType, topic, payload);
    }

    public void markProcessing(Instant now) {
        this.status = OutboxStatus.PROCESSING;
        this.updatedAt = now;
    }

    public void markPublished(Instant now) {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = now;
        this.lastErrorMessage = null;
        this.updatedAt = now;
    }

    public void markFailed(Instant now, Instant nextAttemptAt, String errorMessage) {
        this.status = OutboxStatus.PENDING;
        this.attemptCount += 1;
        this.nextAttemptAt = nextAttemptAt;
        this.lastErrorMessage = trim(errorMessage);
        this.updatedAt = now;
    }

    private String trim(String errorMessage) {
        if (errorMessage == null) {
            return null;
        }
        return errorMessage.length() > 1000 ? errorMessage.substring(0, 1000) : errorMessage;
    }

    public String getId() {
        return id;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getTopic() {
        return topic;
    }

    public String getPayload() {
        return payload;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
