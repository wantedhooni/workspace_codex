package com.example.samplebatchquartzdashboard.batchsample;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record BatchSampleSeedRequest(
        @Min(1) @Max(1_000_000) int size,
        boolean truncateBeforeLoad
) {
}
