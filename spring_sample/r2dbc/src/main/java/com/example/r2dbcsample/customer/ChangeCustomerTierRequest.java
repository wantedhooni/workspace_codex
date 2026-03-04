package com.example.r2dbcsample.customer;

import jakarta.validation.constraints.NotNull;

public record ChangeCustomerTierRequest(
        @NotNull CustomerTier tier
) {
}
