package com.portal.admin.api;

import static com.portal.admin.dto.StatisticsDtos.*;
import com.portal.admin.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@Tag(name = "Statistics", description = "System statistics and reports")
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/overview")
    @Operation(summary = "Get overview statistics")
    public StatisticsOverviewResponse overview() {
        return statisticsService.overview();
    }

    @GetMapping("/menu-usage")
    @Operation(summary = "Get top path usage")
    public List<PathUsageResponse> menuUsage(@RequestParam(defaultValue = "10") int limit) {
        return statisticsService.menuUsage(limit);
    }

    @GetMapping("/action-usage")
    @Operation(summary = "Get top action usage")
    public List<ActionUsageResponse> actionUsage(@RequestParam(defaultValue = "10") int limit) {
        return statisticsService.actionUsage(limit);
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard statistics")
    public StatisticsDashboardResponse dashboard(@RequestParam(defaultValue = "10") int limit) {
        return statisticsService.dashboard(limit);
    }
}
