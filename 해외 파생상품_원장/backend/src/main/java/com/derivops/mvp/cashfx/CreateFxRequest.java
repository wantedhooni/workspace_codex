package com.derivops.mvp.cashfx;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateFxRequest(
        @NotNull Long accountId,
        @NotBlank String fromCurrency,
        @NotBlank String toCurrency,
        @NotNull @DecimalMin(value = "0.0001") BigDecimal amount,
        @NotBlank String reason
) {
}
