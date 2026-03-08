package com.revy.mvpbanking.account.presentation;

import com.revy.mvpbanking.account.application.AccountQueryService;
import com.revy.mvpbanking.common.api.ApiResponse;
import com.revy.mvpbanking.common.api.PageResponse;
import com.revy.mvpbanking.common.support.CurrentPrincipalProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("user-api")
@RequestMapping("/api/user/accounts")
public class UserAccountController {

    private final AccountQueryService accountQueryService;
    private final CurrentPrincipalProvider currentPrincipalProvider;

    public UserAccountController(
            AccountQueryService accountQueryService,
            CurrentPrincipalProvider currentPrincipalProvider
    ) {
        this.accountQueryService = accountQueryService;
        this.currentPrincipalProvider = currentPrincipalProvider;
    }

    @GetMapping
    public ApiResponse<PageResponse<AccountSummaryResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var principal = currentPrincipalProvider.getCurrentPrincipal();
        return ApiResponse.ok(PageResponse.from(
                accountQueryService.getAccountsForEndUser(principal.getPrincipalId()).stream()
                        .map(AccountSummaryResponse::from)
                        .toList(),
                page,
                size
        ));
    }
}
