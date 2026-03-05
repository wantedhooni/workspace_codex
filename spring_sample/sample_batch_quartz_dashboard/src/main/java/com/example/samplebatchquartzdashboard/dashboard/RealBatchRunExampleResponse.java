package com.example.samplebatchquartzdashboard.dashboard;

import java.util.List;

public record RealBatchRunExampleResponse(
        TaskSeedResponse seed,
        TaskJobLaunchResponse job,
        DashboardOverviewResponse overview,
        List<RecentAuditResponse> recentAudits
) {
}
