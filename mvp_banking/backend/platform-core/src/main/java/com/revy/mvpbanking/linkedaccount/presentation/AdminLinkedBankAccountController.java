package com.revy.mvpbanking.linkedaccount.presentation;

import com.revy.mvpbanking.common.api.ApiResponse;
import com.revy.mvpbanking.linkedaccount.application.LinkedBankAccountService;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("admin-api")
@RequestMapping("/api/admin/linked-bank-accounts")
public class AdminLinkedBankAccountController {

    private final LinkedBankAccountService linkedBankAccountService;

    public AdminLinkedBankAccountController(LinkedBankAccountService linkedBankAccountService) {
        this.linkedBankAccountService = linkedBankAccountService;
    }

    @GetMapping
    public ApiResponse<List<LinkedBankAccountResponse>> list() {
        return ApiResponse.ok(
                linkedBankAccountService.getAdminLinkedBankAccounts().stream()
                        .map(LinkedBankAccountResponse::from)
                        .toList()
        );
    }

    @PostMapping("/{linkedBankAccountId}/activate")
    public ApiResponse<LinkedBankAccountResponse> activate(@PathVariable UUID linkedBankAccountId) {
        return ApiResponse.ok(LinkedBankAccountResponse.from(
                linkedBankAccountService.activate(linkedBankAccountId)
        ));
    }

    @PostMapping("/{linkedBankAccountId}/block")
    public ApiResponse<LinkedBankAccountResponse> block(@PathVariable UUID linkedBankAccountId) {
        return ApiResponse.ok(LinkedBankAccountResponse.from(
                linkedBankAccountService.block(linkedBankAccountId)
        ));
    }
}
