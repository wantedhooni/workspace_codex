package com.example.samplesaga.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Saga 처리 대상 주문의 상태를 보관하는 도메인 엔티티다.
 */
@Entity
public class SagaOrder {

    @Id
    private String id;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String productCode;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SagaOrderStatus status;

    @Column(length = 500)
    private String failureReason;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected SagaOrder() {
    }

    public SagaOrder(String customerId, String productCode, int quantity, BigDecimal unitPrice) {
        Instant now = Instant.now();
        this.id = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.productCode = productCode;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.status = SagaOrderStatus.PENDING;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * 모든 단계가 성공했을 때 주문을 완료 상태로 전환한다.
     */
    public void markCompleted() {
        this.status = SagaOrderStatus.COMPLETED;
        this.failureReason = null;
        this.updatedAt = Instant.now();
    }

    /**
     * 선행 단계에서 실패했을 때 주문을 실패 상태로 전환한다.
     *
     * @param reason 실패 사유
     */
    public void markFailed(String reason) {
        this.status = SagaOrderStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = Instant.now();
    }

    /**
     * 일부 단계 성공 후 보상까지 끝났을 때 주문을 보상 완료 상태로 전환한다.
     *
     * @param reason 보상 사유
     */
    public void markCompensated(String reason) {
        this.status = SagaOrderStatus.COMPENSATED;
        this.failureReason = reason;
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getProductCode() {
        return productCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public SagaOrderStatus getStatus() {
        return status;
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
