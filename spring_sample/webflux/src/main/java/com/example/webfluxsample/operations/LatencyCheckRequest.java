package com.example.webfluxsample.operations;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record LatencyCheckRequest(
        @NotBlank String domain,
        @Min(1) int thresholdMillis
) {
}
