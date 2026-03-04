package com.example.samplerabbitmqintegration.settlement.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record DispatchSettlementRequest(
        @NotBlank String settlementId,
        @NotBlank String bookCode,
        @NotBlank String currency,
        @Positive long amount,
        @NotBlank String counterparty
) {
}
