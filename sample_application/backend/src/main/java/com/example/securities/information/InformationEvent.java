package com.example.securities.information;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "information_event")
public class InformationEvent {

    @Id
    private String id;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String aggregateId;

    @Column(nullable = false, length = 2000)
    private String payload;

    @Column(nullable = false)
    private LocalDateTime confirmedAt;

    protected InformationEvent() {
    }

    public InformationEvent(String eventType, String aggregateId, String payload) {
        this.id = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.payload = payload;
    }

    @PrePersist
    void onCreate() {
        if (confirmedAt == null) {
            confirmedAt = LocalDateTime.now();
        }
    }

    public String getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getPayload() {
        return payload;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }
}
