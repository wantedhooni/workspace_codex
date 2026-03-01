package com.stockadmin.dashboard.dashboard;

import java.time.LocalDate;
import java.util.List;

public record DashboardOverviewResponse(
        String title,
        String subtitle,
        String assumptionNote,
        LocalDate asOfDate,
        List<DashboardMetricCard> headlineMetrics,
        List<DashboardTrendPoint> trendPoints,
        List<DashboardBreakdownRow> breakdownRows
) {
}
