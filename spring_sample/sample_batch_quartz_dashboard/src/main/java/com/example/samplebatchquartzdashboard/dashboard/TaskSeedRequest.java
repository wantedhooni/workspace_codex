package com.example.samplebatchquartzdashboard.dashboard;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record TaskSeedRequest(
        @Min(1) @Max(1_000_000) int size,
        boolean truncateBeforeLoad
) {
}
