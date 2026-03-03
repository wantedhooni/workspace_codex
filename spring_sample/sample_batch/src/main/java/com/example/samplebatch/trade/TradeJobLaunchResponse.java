package com.example.samplebatch.trade;

public record TradeJobLaunchResponse(
        Long executionId,
        String status,
        String trigger
) {
}
