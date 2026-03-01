package com.derivops.mvp.cashfx.dto;
import com.derivops.mvp.cashfx.*;
import com.derivops.mvp.cashfx.api.*;
import com.derivops.mvp.cashfx.application.*;
import com.derivops.mvp.cashfx.infrastructure.*;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateCashRequest(
        @NotNull Long accountId,
        @NotNull CashRequestType type,
        @NotNull @DecimalMin(value = "0.0001") BigDecimal amount,
        @NotBlank String currency,
        @NotBlank String reason,
        RequestPriority priority,
        LocalDate valueDate
) {
}
