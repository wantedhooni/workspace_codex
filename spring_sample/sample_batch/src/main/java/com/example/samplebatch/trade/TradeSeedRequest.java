package com.example.samplebatch.trade;

import jakarta.validation.constraints.Min;

public record TradeSeedRequest(
        @Min(1) int size,
        @Min(100) int batchSize,
        boolean truncateBeforeLoad
) {
}
