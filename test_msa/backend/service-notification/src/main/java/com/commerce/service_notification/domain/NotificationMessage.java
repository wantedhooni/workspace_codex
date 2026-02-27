package com.commerce.service_notification.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "notification_messages")
public class NotificationMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Integer totalAmount;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean readFlag;

    protected NotificationMessage() {}

    public NotificationMessage(String type, Long orderId, Long customerId, Integer totalAmount, String status, Instant createdAt) {
        this.type = type;
        this.orderId = orderId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.readFlag = false;
    }

    public Long getId() { return id; }
    public String getType() { return type; }
    public Long getOrderId() { return orderId; }
    public Long getCustomerId() { return customerId; }
    public Integer getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isReadFlag() { return readFlag; }

    public void markRead() { this.readFlag = true; }
}
