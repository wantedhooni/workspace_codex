package com.example.samplemodulith.sales.application;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record PlaceOrderRequest(
        @NotBlank String customerId,
        @NotBlank String sku,
        @Min(1) int quantity,
        @DecimalMin("0.01") BigDecimal unitPrice
) {
}
