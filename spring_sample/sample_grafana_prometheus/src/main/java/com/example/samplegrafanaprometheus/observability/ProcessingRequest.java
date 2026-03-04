package com.example.samplegrafanaprometheus.observability;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ProcessingRequest(
        @NotBlank String channel,
        @NotBlank String workType,
        @Positive long volume,
        @Positive long expectedDurationMs
) {
}
