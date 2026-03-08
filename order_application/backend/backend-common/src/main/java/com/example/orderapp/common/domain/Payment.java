package com.example.orderapp.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

    @Column(nullable = false)
    private Long orderId;

    @Column(length = 128)
    private String externalTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentStatus status;

    private Instant approvedAt;

    private Instant canceledAt;

    protected Payment() {
    }

    public Payment(Long orderId) {
        this.orderId = orderId;
        this.status = PaymentStatus.REQUESTED;
    }

    public void approve(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
        this.status = PaymentStatus.APPROVED;
        this.approvedAt = Instant.now();
    }

    public void markUnknown() {
        this.status = PaymentStatus.UNKNOWN;
    }

    public void fail() {
        this.status = PaymentStatus.FAILED;
    }

    public void cancel() {
        this.status = PaymentStatus.CANCELED;
        this.canceledAt = Instant.now();
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public Instant getCanceledAt() {
        return canceledAt;
    }
}
