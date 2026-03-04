package com.revy.mvpbanking.account.presentation;

import com.revy.mvpbanking.account.application.AccountQueryService;
import com.revy.mvpbanking.common.api.ApiResponse;
import com.revy.mvpbanking.common.support.CurrentPrincipalProvider;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ApiResponse<List<AccountSummaryResponse>> list() {
        var principal = currentPrincipalProvider.getCurrentPrincipal();
        return ApiResponse.ok(
                accountQueryService.getAccountsForEndUser(principal.getPrincipalId()).stream()
                        .map(AccountSummaryResponse::from)
                        .toList()
        );
    }
}
