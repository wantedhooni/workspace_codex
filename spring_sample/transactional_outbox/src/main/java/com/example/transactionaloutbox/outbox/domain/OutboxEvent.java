package com.example.transactionaloutbox.outbox.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import java.time.Instant;
import java.util.UUID;

/**
 * Kafka 발행 전 메시지를 저장하는 Outbox 엔티티다.
 */
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

    /**
     * 신규 Outbox 이벤트를 발행 대기 상태로 생성한다.
     *
     * @param aggregateType 애그리게이트 타입
     * @param aggregateId 애그리게이트 식별자
     * @param eventType 이벤트 타입
     * @param topic 발행 대상 토픽
     * @param payload 직렬화된 이벤트 본문
     * @return 신규 Outbox 엔티티
     */
    public static OutboxEvent pending(
            String aggregateType,
            String aggregateId,
            String eventType,
            String topic,
            String payload
    ) {
        return new OutboxEvent(aggregateType, aggregateId, eventType, topic, payload);
    }

    /**
     * 이벤트를 현재 처리 중 상태로 전환한다.
     *
     * @param now 상태 반영 시각
     */
    public void markProcessing(Instant now) {
        this.status = OutboxStatus.PROCESSING;
        this.updatedAt = now;
    }

    /**
     * 이벤트를 발행 완료 상태로 전환한다.
     *
     * @param now 상태 반영 시각
     */
    public void markPublished(Instant now) {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = now;
        this.lastErrorMessage = null;
        this.updatedAt = now;
    }

    /**
     * 이벤트 발행 실패 시 재시도 가능 상태로 되돌린다.
     *
     * @param now 실패 반영 시각
     * @param nextAttemptAt 다음 재시도 시각
     * @param errorMessage 오류 메시지
     */
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
