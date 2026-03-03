package com.example.sampleredisson.lock;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record LockExecutionRequest(
        @PositiveOrZero long waitMillis,
        @Positive long leaseMillis,
        @Positive long processingMillis
) {
}
