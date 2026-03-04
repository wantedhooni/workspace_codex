package com.example.sampleobservabilityapi.observability;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ProcessingRequest(
        @NotBlank String workloadId,
        @Min(1) int units,
        boolean simulateFailure
) {
}
