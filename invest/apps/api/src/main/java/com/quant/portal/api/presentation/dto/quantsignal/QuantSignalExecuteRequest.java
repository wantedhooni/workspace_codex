package com.quant.portal.api.presentation.dto.quantsignal;

import com.quant.portal.domain.portfolio.enums.CurrencyCode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

public record QuantSignalExecuteRequest(
        @NotNull @Positive Long portfolioId,
        @NotNull @Positive BigDecimal quantity,
        @NotNull @Positive BigDecimal unitPrice,
        LocalDate tradeDate,
        @PositiveOrZero BigDecimal fee,
        @PositiveOrZero BigDecimal tax,
        CurrencyCode currencyCode,
        String memo
) {
}
