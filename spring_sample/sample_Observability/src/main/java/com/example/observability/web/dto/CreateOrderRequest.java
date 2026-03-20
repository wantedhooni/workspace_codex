package com.example.observability.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CreateOrderRequest(
        @NotBlank String orderNumber,
        @NotBlank String title,
        @NotBlank String customerName,
        @NotBlank String priority,
        @NotNull @DecimalMin("0.0") BigDecimal amount,
        OffsetDateTime dueAt
) {
}
