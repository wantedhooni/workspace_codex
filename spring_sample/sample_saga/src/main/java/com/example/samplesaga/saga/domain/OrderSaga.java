package com.example.samplesaga.saga.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.time.Instant;
import java.util.UUID;

@Entity
public class OrderSaga {

    @Id
    private String sagaId;

    @Column(nullable = false)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SagaStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SagaStepStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SagaStepStatus inventoryStatus;

    @Column(length = 500)
    private String failureReason;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected OrderSaga() {
    }

    public OrderSaga(String orderId) {
        Instant now = Instant.now();
        this.sagaId = UUID.randomUUID().toString();
        this.orderId = orderId;
        this.status = SagaStatus.STARTED;
        this.paymentStatus = SagaStepStatus.PENDING;
        this.inventoryStatus = SagaStepStatus.PENDING;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void markPaymentCompleted() {
        this.status = SagaStatus.PAYMENT_COMPLETED;
        this.paymentStatus = SagaStepStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void markInventoryReserved() {
        this.status = SagaStatus.INVENTORY_RESERVED;
        this.inventoryStatus = SagaStepStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void markCompleted() {
        this.status = SagaStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void markFailed(String reason) {
        this.status = SagaStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = Instant.now();
    }

    public void markCompensated(String reason) {
        this.status = SagaStatus.COMPENSATED;
        this.inventoryStatus = SagaStepStatus.FAILED;
        this.paymentStatus = SagaStepStatus.COMPENSATED;
        this.failureReason = reason;
        this.updatedAt = Instant.now();
    }

    public String getSagaId() {
        return sagaId;
    }

    public String getOrderId() {
        return orderId;
    }

    public SagaStatus getStatus() {
        return status;
    }

    public SagaStepStatus getPaymentStatus() {
        return paymentStatus;
    }

    public SagaStepStatus getInventoryStatus() {
        return inventoryStatus;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
