package com.quant.portal.api.presentation.dto.transaction;

import com.quant.portal.domain.portfolio.enums.CurrencyCode;
import com.quant.portal.domain.portfolio.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionCreateRequest(
        @NotNull @Positive Long portfolioId,
        @Positive Long instrumentId,
        @NotNull TransactionType transactionType,
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
