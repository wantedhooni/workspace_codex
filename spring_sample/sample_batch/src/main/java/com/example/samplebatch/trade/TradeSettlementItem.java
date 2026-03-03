package com.example.samplebatch.trade;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TradeSettlementItem(
        long sourceEventId,
        String accountNo,
        String instrumentCode,
        BigDecimal grossAmount,
        BigDecimal feeAmount,
        BigDecimal netAmount,
        LocalDate settlementDate
) {
}
