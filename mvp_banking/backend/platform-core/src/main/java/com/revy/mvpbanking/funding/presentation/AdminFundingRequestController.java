package com.revy.mvpbanking.funding.presentation;

import com.revy.mvpbanking.common.api.ApiResponse;
import com.revy.mvpbanking.funding.application.FundingRequestService;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("admin-api")
@RequestMapping("/api/admin/funding-requests")
public class AdminFundingRequestController {

    private final FundingRequestService fundingRequestService;

    public AdminFundingRequestController(FundingRequestService fundingRequestService) {
        this.fundingRequestService = fundingRequestService;
    }

    @GetMapping
    public ApiResponse<List<FundingRequestResponse>> list() {
        return ApiResponse.ok(fundingRequestService.getAdminRequests().stream().map(FundingRequestResponse::from).toList());
    }
}
