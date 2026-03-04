package com.revy.mvpbanking.funding.presentation;

import com.revy.mvpbanking.common.api.ApiResponse;
import com.revy.mvpbanking.common.support.CurrentPrincipalProvider;
import com.revy.mvpbanking.funding.application.FundingRequestService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("user-api")
@RequestMapping("/api/user/funding-requests")
public class UserFundingRequestController {

    private final FundingRequestService fundingRequestService;
    private final CurrentPrincipalProvider currentPrincipalProvider;

    public UserFundingRequestController(
            FundingRequestService fundingRequestService,
            CurrentPrincipalProvider currentPrincipalProvider
    ) {
        this.fundingRequestService = fundingRequestService;
        this.currentPrincipalProvider = currentPrincipalProvider;
    }

    @GetMapping
    public ApiResponse<List<FundingRequestResponse>> list() {
        var principal = currentPrincipalProvider.getCurrentPrincipal();
        return ApiResponse.ok(fundingRequestService.getUserRequests(principal.getPrincipalId()).stream().map(FundingRequestResponse::from).toList());
    }

    @PostMapping
    public ApiResponse<FundingRequestResponse> create(@Valid @RequestBody CreateFundingRequest request) {
        var principal = currentPrincipalProvider.getCurrentPrincipal();
        return ApiResponse.ok(FundingRequestResponse.from(
                fundingRequestService.create(
                        principal.getPrincipalId(),
                        request.accountId(),
                        request.requestType(),
                        request.amount(),
                        request.note()
                )
        ));
    }
}
