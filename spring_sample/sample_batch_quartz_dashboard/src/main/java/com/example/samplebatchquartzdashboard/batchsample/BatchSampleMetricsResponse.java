package com.example.samplebatchquartzdashboard.batchsample;

public record BatchSampleMetricsResponse(
        long totalInput,
        long pendingInput,
        long processedInput,
        long outputCount,
        long batchExecutionCount,
        String lastJobStatus,
        int chunkSize,
        int pageSize
) {
}
