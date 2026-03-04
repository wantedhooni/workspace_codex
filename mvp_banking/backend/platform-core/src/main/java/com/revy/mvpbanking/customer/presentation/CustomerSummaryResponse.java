package com.revy.mvpbanking.customer.presentation;

import com.revy.mvpbanking.common.support.MaskingUtils;
import com.revy.mvpbanking.customer.domain.Customer;
import java.time.Instant;
import java.util.UUID;

public record CustomerSummaryResponse(
        UUID id,
        String customerNumber,
        String fullName,
        String email,
        String status,
        Instant createdAt
) {
    public static CustomerSummaryResponse from(Customer customer) {
        return new CustomerSummaryResponse(
                customer.getId(),
                customer.getCustomerNumber(),
                MaskingUtils.maskName(customer.getFullName()),
                MaskingUtils.maskEmail(customer.getEmail()),
                customer.getStatus().name(),
                customer.getCreatedAt()
        );
    }
}
