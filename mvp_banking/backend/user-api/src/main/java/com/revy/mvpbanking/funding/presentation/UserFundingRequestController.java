package com.revy.mvpbanking.funding.presentation;

import com.revy.mvpbanking.common.api.ApiResponse;
import com.revy.mvpbanking.common.api.PageResponse;
import com.revy.mvpbanking.common.support.CurrentPrincipalProvider;
import com.revy.mvpbanking.funding.application.FundingRequestService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ApiResponse<PageResponse<FundingRequestResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var principal = currentPrincipalProvider.getCurrentPrincipal();
        return ApiResponse.ok(PageResponse.from(
                fundingRequestService.getUserRequests(principal.getPrincipalId()).stream().map(FundingRequestResponse::from).toList(),
                page,
                size
        ));
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
                        request.linkedBankAccountId(),
                        request.priorityProcessing(),
                        request.note()
                )
        ));
    }

    @PostMapping("/{requestId}/cancel")
    public ApiResponse<FundingRequestResponse> cancel(
            @PathVariable UUID requestId,
            @Valid @RequestBody(required = false) CancelFundingRequest request
    ) {
        var principal = currentPrincipalProvider.getCurrentPrincipal();
        String reason = request == null ? null : request.reason();
        return ApiResponse.ok(FundingRequestResponse.from(
                fundingRequestService.cancelByUser(principal.getPrincipalId(), requestId, reason)
        ));
    }
}
