package com.stockadmin.dashboard.dashboard;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardOverviewController {

    private final DashboardOverviewService dashboardOverviewService;

    public DashboardOverviewController(DashboardOverviewService dashboardOverviewService) {
        this.dashboardOverviewService = dashboardOverviewService;
    }

    @GetMapping("/overview")
    public DashboardOverviewResponse overview() {
        return dashboardOverviewService.getOverview();
    }
}
