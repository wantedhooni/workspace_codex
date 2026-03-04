package com.example.samplebatchquartzdashboard.dashboard;

import java.time.Instant;

public record TaskImportAuditItem(
        long requestId,
        String externalId,
        int payloadSize,
        long processingLatencyMs,
        Instant createdAt
) {
}
