package com.example.r2dbcsample.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCustomerRequest(
        @NotBlank String customerCode,
        @NotBlank String name,
        @Email String email,
        @NotNull CustomerTier tier
) {
}
