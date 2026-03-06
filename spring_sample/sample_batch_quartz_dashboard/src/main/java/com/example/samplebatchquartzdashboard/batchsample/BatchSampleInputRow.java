package com.example.samplebatchquartzdashboard.batchsample;

import java.math.BigDecimal;
import java.time.Instant;

public record BatchSampleInputRow(
        long id,
        String accountNo,
        String instrumentCode,
        BigDecimal quantity,
        BigDecimal unitPrice,
        Instant createdAt
) {
}
