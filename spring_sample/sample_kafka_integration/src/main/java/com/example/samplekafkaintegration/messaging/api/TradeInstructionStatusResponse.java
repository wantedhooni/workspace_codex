package com.example.samplekafkaintegration.messaging.api;

import java.time.Instant;

public record TradeInstructionStatusResponse(
        String messageId,
        String accountId,
        String instrumentCode,
        long quantity,
        String counterparty,
        String status,
        String detail,
        Instant updatedAt
) {
}
