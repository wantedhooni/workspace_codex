package com.revy.mvpbanking.transaction.presentation;

import com.revy.mvpbanking.common.api.ApiResponse;
import com.revy.mvpbanking.common.api.PageResponse;
import com.revy.mvpbanking.common.support.CurrentPrincipalProvider;
import com.revy.mvpbanking.transaction.application.TransactionQueryService;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("user-api")
@RequestMapping("/api/user/transactions")
public class UserTransactionController {

    private final TransactionQueryService transactionQueryService;
    private final CurrentPrincipalProvider currentPrincipalProvider;

    public UserTransactionController(
            TransactionQueryService transactionQueryService,
            CurrentPrincipalProvider currentPrincipalProvider
    ) {
        this.transactionQueryService = transactionQueryService;
        this.currentPrincipalProvider = currentPrincipalProvider;
    }

    @GetMapping
    public ApiResponse<PageResponse<TransactionSummaryResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var principal = currentPrincipalProvider.getCurrentPrincipal();
        return ApiResponse.ok(PageResponse.from(
                transactionQueryService.getTransactionsForEndUser(principal.getPrincipalId()).stream()
                        .map(TransactionSummaryResponse::from)
                        .toList(),
                page,
                size
        ));
    }
}
