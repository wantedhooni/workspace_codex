package com.example.securities.external;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "external_message")
public class ExternalMessage {

    @Id
    private String id;

    @Column(nullable = false)
    private String messageType;

    @Column(nullable = false, length = 2000)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExternalMessageStatus status;

    @Column(nullable = false)
    private int retryCount;

    @Column(length = 500)
    private String lastError;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected ExternalMessage() {
    }

    public ExternalMessage(String messageType, String payload) {
        this.id = UUID.randomUUID().toString();
        this.messageType = messageType;
        this.payload = payload;
        this.status = ExternalMessageStatus.PENDING;
        this.retryCount = 0;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void markSent() {
        this.status = ExternalMessageStatus.SENT;
        this.lastError = null;
    }

    public void markFailed(String error) {
        this.status = ExternalMessageStatus.FAILED;
        this.retryCount += 1;
        this.lastError = error;
    }

    public String getId() {
        return id;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getPayload() {
        return payload;
    }

    public ExternalMessageStatus getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getLastError() {
        return lastError;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
