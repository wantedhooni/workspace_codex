package com.example.samplerabbitmqintegration.settlement.api;

import java.time.Instant;

public record SettlementStatusResponse(
        String settlementId,
        String bookCode,
        String currency,
        long amount,
        String counterparty,
        String status,
        String detail,
        Instant updatedAt
) {
}
