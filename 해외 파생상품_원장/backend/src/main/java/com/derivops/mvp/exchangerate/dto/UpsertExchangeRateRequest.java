package com.derivops.mvp.exchangerate.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UpsertExchangeRateRequest(
        @NotBlank String fromCurrency,
        @NotBlank String toCurrency,
        @NotNull LocalDate rateDate,
        @NotNull @DecimalMin(value = "0.00000001") BigDecimal rate,
        @NotBlank String source
) {
}
