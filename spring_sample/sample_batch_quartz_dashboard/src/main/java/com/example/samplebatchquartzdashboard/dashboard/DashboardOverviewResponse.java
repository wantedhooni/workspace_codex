package com.example.samplebatchquartzdashboard.dashboard;

import java.time.Instant;

public record DashboardOverviewResponse(
        long totalRequests,
        long pendingRequests,
        long processedRequests,
        long auditRows,
        long batchExecutions,
        String lastBatchStatus,
        String quartzTriggerState,
        Instant nextFireTime
) {
}
