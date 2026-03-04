package com.example.r2dbcsample.customer;

import java.time.LocalDateTime;

public record CustomerResponse(
        String customerCode,
        String name,
        String email,
        CustomerTier tier,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    static CustomerResponse from(CustomerAccount customerAccount) {
        return new CustomerResponse(
                customerAccount.getCustomerCode(),
                customerAccount.getName(),
                customerAccount.getEmail(),
                CustomerTier.valueOf(customerAccount.getTier()),
                customerAccount.getCreatedAt(),
                customerAccount.getUpdatedAt()
        );
    }
}
