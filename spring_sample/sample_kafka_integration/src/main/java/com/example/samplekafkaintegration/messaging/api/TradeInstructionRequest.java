package com.example.samplekafkaintegration.messaging.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record TradeInstructionRequest(
        @NotBlank String accountId,
        @NotBlank String instrumentCode,
        @Positive long quantity,
        @NotBlank String counterparty
) {
}
