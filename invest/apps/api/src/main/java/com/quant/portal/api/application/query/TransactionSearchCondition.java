package com.quant.portal.api.application.query;

import com.quant.portal.domain.portfolio.enums.TransactionType;
import java.time.LocalDate;

public record TransactionSearchCondition(
        Long portfolioId,
        Long instrumentId,
        TransactionType transactionType,
        LocalDate fromDate,
        LocalDate toDate
) {
}
