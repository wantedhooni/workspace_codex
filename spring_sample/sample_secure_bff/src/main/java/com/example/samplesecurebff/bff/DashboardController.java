package com.example.samplesecurebff.bff;

import java.security.Principal;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final DashboardAggregationService dashboardAggregationService;

    public DashboardController(DashboardAggregationService dashboardAggregationService) {
        this.dashboardAggregationService = dashboardAggregationService;
    }

    @GetMapping("/bff/accounts/{accountId}/dashboard")
    public AccountDashboardResponse dashboard(@PathVariable String accountId) {
        return dashboardAggregationService.dashboard(accountId);
    }

    @GetMapping("/admin/session")
    public Map<String, String> session(Principal principal) {
        return Map.of("username", principal.getName(), "scope", "admin");
    }
}
