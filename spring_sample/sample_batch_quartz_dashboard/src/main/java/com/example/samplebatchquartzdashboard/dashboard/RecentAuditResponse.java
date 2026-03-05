package com.example.samplebatchquartzdashboard.dashboard;

import java.math.BigDecimal;
import java.time.Instant;

public record RecentAuditResponse(
        long requestId,
        String externalId,
        String sourceSystem,
        String accountNo,
        String instrumentCode,
        String market,
        String settlementCurrency,
        BigDecimal notionalAmount,
        int priority,
        String riskBucket,
        int payloadSize,
        long processingLatencyMs,
        Instant createdAt
) {
}
