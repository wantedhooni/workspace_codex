package com.example.samplekafkaintegration.messaging.domain;

import java.time.Instant;

public record TradeInstructionMessage(
        String messageId,
        String accountId,
        String instrumentCode,
        long quantity,
        String counterparty,
        Instant requestedAt
) {
}
