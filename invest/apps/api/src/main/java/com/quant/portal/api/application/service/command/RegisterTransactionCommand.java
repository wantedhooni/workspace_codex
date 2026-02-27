package com.quant.portal.api.application.service.command;

import com.quant.portal.domain.portfolio.enums.CurrencyCode;
import com.quant.portal.domain.portfolio.enums.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RegisterTransactionCommand(
        Long portfolioId,
        Long instrumentId,
        TransactionType transactionType,
        LocalDate tradeDate,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal amount,
        BigDecimal fee,
        BigDecimal tax,
        CurrencyCode currencyCode,
        String memo
) {
}
