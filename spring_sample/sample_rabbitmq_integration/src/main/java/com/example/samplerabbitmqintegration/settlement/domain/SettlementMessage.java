package com.example.samplerabbitmqintegration.settlement.domain;

import java.time.Instant;

public record SettlementMessage(
        String settlementId,
        String bookCode,
        String currency,
        long amount,
        String counterparty,
        Instant requestedAt
) {
}
