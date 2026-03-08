package com.example.orderapp.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @Column(nullable = false, unique = true, length = 64)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(length = 255)
    private String cancelReason;

    protected Order() {
    }

    public Order(String orderNumber, BigDecimal amount) {
        this.orderNumber = orderNumber;
        this.amount = amount;
        this.status = OrderStatus.CREATED;
    }

    public void markPendingConfirm() {
        this.status = OrderStatus.PENDING_CONFIRM;
    }

    public void confirm() {
        this.status = OrderStatus.CONFIRMED;
    }

    public void fail() {
        this.status = OrderStatus.FAILED;
    }

    public void cancel(String reason) {
        this.status = OrderStatus.CANCELED;
        this.cancelReason = reason;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCancelReason() {
        return cancelReason;
    }
}
