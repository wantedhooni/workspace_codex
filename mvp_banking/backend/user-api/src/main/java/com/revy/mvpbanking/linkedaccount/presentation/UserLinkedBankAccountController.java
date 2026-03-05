package com.revy.mvpbanking.linkedaccount.presentation;

import com.revy.mvpbanking.common.api.ApiResponse;
import com.revy.mvpbanking.common.support.CurrentPrincipalProvider;
import com.revy.mvpbanking.linkedaccount.application.LinkedBankAccountService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("user-api")
@RequestMapping("/api/user/linked-bank-accounts")
public class UserLinkedBankAccountController {

    private final LinkedBankAccountService linkedBankAccountService;
    private final CurrentPrincipalProvider currentPrincipalProvider;

    public UserLinkedBankAccountController(
            LinkedBankAccountService linkedBankAccountService,
            CurrentPrincipalProvider currentPrincipalProvider
    ) {
        this.linkedBankAccountService = linkedBankAccountService;
        this.currentPrincipalProvider = currentPrincipalProvider;
    }

    @GetMapping
    public ApiResponse<List<LinkedBankAccountResponse>> list() {
        var principal = currentPrincipalProvider.getCurrentPrincipal();
        return ApiResponse.ok(
                linkedBankAccountService.getUserLinkedBankAccounts(principal.getPrincipalId()).stream()
                        .map(LinkedBankAccountResponse::from)
                        .toList()
        );
    }

    @PostMapping
    public ApiResponse<LinkedBankAccountResponse> create(@Valid @RequestBody CreateLinkedBankAccountRequest request) {
        var principal = currentPrincipalProvider.getCurrentPrincipal();
        return ApiResponse.ok(LinkedBankAccountResponse.from(
                linkedBankAccountService.create(
                        principal.getPrincipalId(),
                        request.bankName(),
                        request.accountAlias(),
                        request.accountHolderName(),
                        request.accountNumber(),
                        request.primaryWithdrawal()
                )
        ));
    }

    @PostMapping("/{linkedBankAccountId}/primary")
    public ApiResponse<LinkedBankAccountResponse> markPrimary(@PathVariable UUID linkedBankAccountId) {
        var principal = currentPrincipalProvider.getCurrentPrincipal();
        return ApiResponse.ok(LinkedBankAccountResponse.from(
                linkedBankAccountService.markPrimary(principal.getPrincipalId(), linkedBankAccountId)
        ));
    }

    @PostMapping("/{linkedBankAccountId}/verify")
    public ApiResponse<LinkedBankAccountResponse> verify(
            @PathVariable UUID linkedBankAccountId,
            @Valid @RequestBody VerifyLinkedBankAccountRequest request
    ) {
        var principal = currentPrincipalProvider.getCurrentPrincipal();
        return ApiResponse.ok(LinkedBankAccountResponse.from(
                linkedBankAccountService.verify(
                        principal.getPrincipalId(),
                        linkedBankAccountId,
                        request.verificationReference()
                )
        ));
    }

    @PostMapping("/{linkedBankAccountId}/resend-verification")
    public ApiResponse<LinkedBankAccountResponse> resendVerification(@PathVariable UUID linkedBankAccountId) {
        var principal = currentPrincipalProvider.getCurrentPrincipal();
        return ApiResponse.ok(LinkedBankAccountResponse.from(
                linkedBankAccountService.resendVerification(
                        principal.getPrincipalId(),
                        linkedBankAccountId
                )
        ));
    }
}
