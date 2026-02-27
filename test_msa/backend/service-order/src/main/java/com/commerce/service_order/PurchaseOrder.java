package com.commerce.service_order;

import jakarta.persistence.*;

@Entity
@Table(name = "orders")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Integer totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    protected PurchaseOrder() {}

    public PurchaseOrder(Long customerId, Integer totalAmount, OrderStatus status) {
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public Integer getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }

    public void setStatus(OrderStatus status) { this.status = status; }
}
