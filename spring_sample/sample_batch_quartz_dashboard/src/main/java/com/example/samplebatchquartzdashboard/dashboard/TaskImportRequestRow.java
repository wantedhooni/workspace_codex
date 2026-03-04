package com.example.samplebatchquartzdashboard.dashboard;

import java.time.Instant;

public record TaskImportRequestRow(
        long id,
        String externalId,
        int payloadSize,
        Instant requestedAt
) {
}
