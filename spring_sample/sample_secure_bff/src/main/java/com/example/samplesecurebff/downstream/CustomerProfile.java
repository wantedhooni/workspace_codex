package com.example.samplesecurebff.downstream;

public record CustomerProfile(
        String accountId,
        String customerName,
        String segment,
        String baseCurrency
) {
}
