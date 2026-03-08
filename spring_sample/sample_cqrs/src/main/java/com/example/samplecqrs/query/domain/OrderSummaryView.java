package com.example.samplecqrs.query.domain;

import com.example.samplecqrs.command.domain.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 조회 전용으로 비정규화한 주문 요약 프로젝션이다.
 */
@Entity
public class OrderSummaryView {

    @Id
    private String orderId;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String productCode;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected OrderSummaryView() {
    }

    public OrderSummaryView(
            String orderId,
            String customerId,
            String productCode,
            int quantity,
            BigDecimal totalAmount,
            OrderStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.productCode = productCode;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 조회 모델의 주문 상태를 취소로 전환한다.
     *
     * @param updatedAt 상태 반영 시각
     */
    public void markCancelled(Instant updatedAt) {
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = updatedAt;
    }

    public String getOrderId() {
        return orderId;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
