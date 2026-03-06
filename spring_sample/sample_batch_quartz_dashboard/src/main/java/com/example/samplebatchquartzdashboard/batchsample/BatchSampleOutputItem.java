package com.example.samplebatchquartzdashboard.batchsample;

import java.math.BigDecimal;

public record BatchSampleOutputItem(
        long inputId,
        String accountNo,
        String instrumentCode,
        BigDecimal grossAmount,
        BigDecimal feeAmount,
        BigDecimal netAmount,
        String riskGrade
) {
}
