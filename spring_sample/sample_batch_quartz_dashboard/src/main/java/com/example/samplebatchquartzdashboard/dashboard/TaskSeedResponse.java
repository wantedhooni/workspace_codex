package com.example.samplebatchquartzdashboard.dashboard;

public record TaskSeedResponse(
        int loadedCount,
        long pendingCount
) {
}
