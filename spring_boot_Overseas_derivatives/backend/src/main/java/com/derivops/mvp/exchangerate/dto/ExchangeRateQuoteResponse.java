package com.derivops.mvp.exchangerate.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExchangeRateQuoteResponse(
        String fromCurrency,
        String toCurrency,
        BigDecimal requestedAmount,
        BigDecimal exchangeRate,
        BigDecimal convertedAmount,
        LocalDate rateDate,
        String source,
        String quoteMode
) {
}
