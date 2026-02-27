package com.quant.portal.api.presentation.dto.transaction;

import com.quant.portal.domain.portfolio.enums.CurrencyCode;
import com.quant.portal.domain.portfolio.enums.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        Long portfolioId,
        Long instrumentId,
        TransactionType transactionType,
        LocalDate tradeDate,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal amount,
        BigDecimal fee,
        BigDecimal tax,
        BigDecimal cashImpact,
        BigDecimal realizedPnl,
        CurrencyCode currencyCode,
        String memo
) {
}
