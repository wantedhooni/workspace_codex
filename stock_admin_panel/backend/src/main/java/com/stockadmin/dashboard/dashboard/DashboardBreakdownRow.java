package com.stockadmin.dashboard.dashboard;

public record DashboardBreakdownRow(
        String segment,
        String owner,
        String status,
        long orders,
        String notional,
        long pendingIssues
) {
}
