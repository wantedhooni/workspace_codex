package com.portal.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public final class StatisticsDtos {
    private StatisticsDtos() {}

    @Schema(name = "StatisticsOverviewResponse", description = "High-level statistics snapshot")
    public record StatisticsOverviewResponse(
            @Schema(description = "Total admin users")
            long totalAdmins,
            @Schema(description = "Total roles")
            long totalRoles,
            @Schema(description = "Total permissions")
            long totalPermissions,
            @Schema(description = "Total menus")
            long totalMenus,
            @Schema(description = "Total programs")
            long totalPrograms,
            @Schema(description = "Total contents")
            long totalContents,
            @Schema(description = "Total access logs")
            long totalAccessLogs,
            @Schema(description = "Total service audit logs")
            long totalServiceAuditLogs,
            @Schema(description = "Today's request count")
            long todayRequests,
            @Schema(description = "Today's successful login count")
            long todaySuccessfulLogins
    ) {}

    @Schema(name = "PathUsageResponse", description = "Top path usage")
    public record PathUsageResponse(
            @Schema(description = "Request path", example = "/admins")
            String path,
            @Schema(description = "Call count", example = "120")
            long total
    ) {}

    @Schema(name = "ActionUsageResponse", description = "Top action usage")
    public record ActionUsageResponse(
            @Schema(description = "Action name", example = "GET")
            String action,
            @Schema(description = "Call count", example = "320")
            long total
    ) {}

    @Schema(name = "StatisticsDashboardResponse", description = "Dashboard statistics")
    public record StatisticsDashboardResponse(
            @Schema(description = "Overview section")
            StatisticsOverviewResponse overview,
            @Schema(description = "Top path usage section")
            List<PathUsageResponse> topPaths,
            @Schema(description = "Top action usage section")
            List<ActionUsageResponse> topActions
    ) {}
}
