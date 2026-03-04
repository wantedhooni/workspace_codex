package com.revy.mvpbanking.customer.domain;

import com.revy.mvpbanking.common.domain.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "customers")
public class Customer extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "end_user_id", unique = true)
    private UUID endUserId;

    @Column(name = "customer_number", nullable = false, unique = true, length = 40)
    private String customerNumber;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CustomerStatus status;

    protected Customer() {
    }

    public Customer(UUID endUserId, String customerNumber, String fullName, String email, CustomerStatus status) {
        this.endUserId = endUserId;
        this.customerNumber = customerNumber;
        this.fullName = fullName;
        this.email = email;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEndUserId() {
        return endUserId;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public CustomerStatus getStatus() {
        return status;
    }
}
