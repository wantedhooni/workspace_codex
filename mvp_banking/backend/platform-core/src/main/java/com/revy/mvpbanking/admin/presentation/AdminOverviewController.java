package com.revy.mvpbanking.admin.presentation;

import com.revy.mvpbanking.admin.application.AdminOverviewService;
import com.revy.mvpbanking.common.api.ApiResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("admin-api")
@RequestMapping("/api/admin/overview")
public class AdminOverviewController {

    private final AdminOverviewService adminOverviewService;

    public AdminOverviewController(AdminOverviewService adminOverviewService) {
        this.adminOverviewService = adminOverviewService;
    }

    @GetMapping
    public ApiResponse<AdminOverviewResponse> overview() {
        return ApiResponse.ok(adminOverviewService.getOverview());
    }
}
