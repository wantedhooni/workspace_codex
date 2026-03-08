package com.example.samplesaga.order.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record CreateSagaOrderRequest(
        @NotBlank String customerId,
        @NotBlank String productCode,
        @Min(1) int quantity,
        @DecimalMin(value = "0.01") BigDecimal unitPrice
) {
}
