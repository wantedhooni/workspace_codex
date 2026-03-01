package com.derivops.mvp.exchangerate.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ExchangeRateResponse(
        Long id,
        String fromCurrency,
        String toCurrency,
        LocalDate rateDate,
        BigDecimal rate,
        String source,
        OffsetDateTime createdAt
) {
}
