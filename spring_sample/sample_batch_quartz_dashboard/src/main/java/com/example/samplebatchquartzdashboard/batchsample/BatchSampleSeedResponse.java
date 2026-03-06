package com.example.samplebatchquartzdashboard.batchsample;

public record BatchSampleSeedResponse(
        int loadedCount,
        long pendingCount,
        int chunkSize,
        int pageSize
) {
}
