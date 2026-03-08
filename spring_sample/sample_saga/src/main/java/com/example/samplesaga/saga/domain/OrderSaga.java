package com.example.samplesaga.saga.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.time.Instant;
import java.util.UUID;

/**
 * 주문 Saga의 전체 실행 상태와 단계별 상태를 저장하는 엔티티다.
 */
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

    /**
     * 결제 단계를 완료 상태로 반영한다.
     */
    public void markPaymentCompleted() {
        this.status = SagaStatus.PAYMENT_COMPLETED;
        this.paymentStatus = SagaStepStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    /**
     * 재고 예약 단계를 완료 상태로 반영한다.
     */
    public void markInventoryReserved() {
        this.status = SagaStatus.INVENTORY_RESERVED;
        this.inventoryStatus = SagaStepStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    /**
     * Saga 전체를 완료 상태로 전환한다.
     */
    public void markCompleted() {
        this.status = SagaStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    /**
     * Saga를 실패 상태로 전환한다.
     *
     * @param reason 실패 사유
     */
    public void markFailed(String reason) {
        this.status = SagaStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = Instant.now();
    }

    /**
     * 보상 트랜잭션까지 완료된 Saga로 전환한다.
     *
     * @param reason 보상 사유
     */
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
