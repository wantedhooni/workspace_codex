package com.example.r2dbcsample.customer;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("customer_account")
public class CustomerAccount {

    @Id
    private Long id;
    private String customerCode;
    private String name;
    private String email;
    private String tier;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CustomerAccount() {
    }

    public CustomerAccount(
            Long id,
            String customerCode,
            String name,
            String email,
            String tier,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.customerCode = customerCode;
        this.name = name;
        this.email = email;
        this.tier = tier;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CustomerAccount create(String customerCode, String name, String email, CustomerTier tier) {
        LocalDateTime now = LocalDateTime.now();
        return new CustomerAccount(null, customerCode, name, email, tier.name(), now, now);
    }

    public CustomerAccount withTier(CustomerTier tier) {
        return new CustomerAccount(id, customerCode, name, email, tier.name(), createdAt, LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getTier() {
        return tier;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
