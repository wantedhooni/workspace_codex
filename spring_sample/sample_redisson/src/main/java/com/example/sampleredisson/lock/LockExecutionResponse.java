package com.example.sampleredisson.lock;

import java.time.Instant;

public record LockExecutionResponse(
        String name,
        long before,
        long after,
        Instant executedAt
) {
}
