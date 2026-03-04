package com.revy.mvpbanking.customer.presentation;

import com.revy.mvpbanking.customer.domain.AdminCustomerQueryRepository;

public record CustomerStatusSummaryResponse(
        long active,
        long reviewRequired,
        long suspended,
        long total
) {
    public static CustomerStatusSummaryResponse from(AdminCustomerQueryRepository.CustomerStatusSummary summary) {
        return new CustomerStatusSummaryResponse(
                summary.active(),
                summary.reviewRequired(),
                summary.suspended(),
                summary.total()
        );
    }
}
