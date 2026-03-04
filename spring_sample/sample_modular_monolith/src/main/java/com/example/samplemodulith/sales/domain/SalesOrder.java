package com.example.samplemodulith.sales.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
public class SalesOrder {

    @Id
    private String id;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    protected SalesOrder() {
    }

    public SalesOrder(String customerId, String sku, int quantity, BigDecimal unitPrice) {
        this.id = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.sku = sku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.status = OrderStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void markReserved() {
        this.status = OrderStatus.RESERVED;
    }

    public void markInvoiced() {
        this.status = OrderStatus.INVOICED;
    }

    public void markRejected() {
        this.status = OrderStatus.REJECTED;
    }
}
