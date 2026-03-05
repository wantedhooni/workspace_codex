package com.revy.mvpbanking.transaction.presentation;

import com.revy.mvpbanking.common.api.ApiResponse;
import com.revy.mvpbanking.common.support.CurrentPrincipalProvider;
import com.revy.mvpbanking.transaction.application.TransactionQueryService;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ApiResponse<List<TransactionSummaryResponse>> list() {
        var principal = currentPrincipalProvider.getCurrentPrincipal();
        return ApiResponse.ok(
                transactionQueryService.getTransactionsForEndUser(principal.getPrincipalId()).stream()
                        .map(TransactionSummaryResponse::from)
                        .toList()
        );
    }
}
