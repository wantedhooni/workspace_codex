package com.example.sampleeventdriven.order.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record PlaceOrderRequest(
        @NotBlank String accountId,
        @DecimalMin("0.01") BigDecimal amount,
        @NotBlank String idempotencyKey
) {
}
