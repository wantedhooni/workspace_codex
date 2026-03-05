package com.example.samplebatchquartzdashboard.dashboard;

import java.math.BigDecimal;
import java.time.Instant;

public record TaskImportRequestRow(
        long id,
        String externalId,
        String sourceSystem,
        String accountNo,
        String instrumentCode,
        String market,
        String settlementCurrency,
        BigDecimal notionalAmount,
        int payloadSize,
        int priority,
        Instant requestedAt
) {
}
