package com.quant.portal.api.presentation.dto.transaction;

import com.quant.portal.domain.portfolio.enums.CurrencyCode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionUpdateRequest(
        @Positive Long instrumentId,
        @NotNull LocalDate tradeDate,
        @Positive BigDecimal quantity,
        @Positive BigDecimal unitPrice,
        @Positive BigDecimal amount,
        @PositiveOrZero BigDecimal fee,
        @PositiveOrZero BigDecimal tax,
        CurrencyCode currencyCode,
        String memo
) {
}
