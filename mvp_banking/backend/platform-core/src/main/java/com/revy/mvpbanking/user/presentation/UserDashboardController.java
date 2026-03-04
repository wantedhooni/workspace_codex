package com.revy.mvpbanking.user.presentation;

import com.revy.mvpbanking.common.api.ApiResponse;
import com.revy.mvpbanking.common.support.CurrentPrincipalProvider;
import com.revy.mvpbanking.user.application.UserDashboardInsightService;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("user-api")
@RequestMapping("/api/user/dashboard")
public class UserDashboardController {

    private final UserDashboardInsightService userDashboardInsightService;
    private final CurrentPrincipalProvider currentPrincipalProvider;

    public UserDashboardController(
            UserDashboardInsightService userDashboardInsightService,
            CurrentPrincipalProvider currentPrincipalProvider
    ) {
        this.userDashboardInsightService = userDashboardInsightService;
        this.currentPrincipalProvider = currentPrincipalProvider;
    }

    @GetMapping("/insights")
    public ApiResponse<UserDashboardInsightResponse> insights() {
        var principal = currentPrincipalProvider.getCurrentPrincipal();
        return ApiResponse.ok(userDashboardInsightService.getInsights(principal.getPrincipalId()));
    }
}
