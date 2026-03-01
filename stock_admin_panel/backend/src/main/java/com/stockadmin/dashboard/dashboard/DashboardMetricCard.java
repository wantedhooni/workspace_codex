package com.stockadmin.dashboard.dashboard;

public record DashboardMetricCard(
        String key,
        String label,
        String value,
        String deltaLabel,
        double deltaRate,
        String tone
) {
}
