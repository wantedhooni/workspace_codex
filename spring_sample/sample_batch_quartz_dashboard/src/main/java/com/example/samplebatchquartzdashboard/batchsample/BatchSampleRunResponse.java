package com.example.samplebatchquartzdashboard.batchsample;

public record BatchSampleRunResponse(
        long jobExecutionId,
        String status,
        long processedCount,
        long outputCount
) {
}
