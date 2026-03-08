package com.example.samplesaga.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.time.Instant;
import java.util.UUID;

/**
 * 재고 예약 결과를 저장하는 엔티티다.
 */
@Entity
public class InventoryReservation {

    @Id
    private String reservationId;

    @Column(nullable = false, unique = true)
    private String orderId;

    @Column(nullable = false)
    private String productCode;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryReservationStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    protected InventoryReservation() {
    }

    public InventoryReservation(String orderId, String productCode, int quantity) {
        this.reservationId = UUID.randomUUID().toString();
        this.orderId = orderId;
        this.productCode = productCode;
        this.quantity = quantity;
        this.status = InventoryReservationStatus.RESERVED;
        this.createdAt = Instant.now();
    }

    public String getReservationId() {
        return reservationId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getProductCode() {
        return productCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public InventoryReservationStatus getStatus() {
        return status;
    }
}
