package com.example.securities.account;

import com.example.securities.account.AccountDtos.AccountResponse;
import com.example.securities.account.AccountDtos.LedgerEntryView;
import com.example.securities.account.AccountDtos.LedgerSearchRequest;
import com.example.securities.account.AccountDtos.OpenAccountRequest;
import com.example.securities.account.AccountDtos.TransactionRequest;
import com.example.securities.account.AccountDtos.TransactionResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PreAuthorize("hasRole('OPERATOR')")
    @PostMapping
    public AccountResponse openAccount(@RequestBody OpenAccountRequest request) {
        return accountService.openAccount(request.customerId());
    }

    @PreAuthorize("hasAnyRole('VIEWER','OPERATOR')")
    @GetMapping
    public List<AccountResponse> getAccounts() {
        return accountService.getAccounts();
    }

    @PreAuthorize("hasRole('OPERATOR')")
    @PostMapping("/{accountId}/transactions")
    public TransactionResponse transact(
            @PathVariable String accountId,
            @RequestBody TransactionRequest request,
            @RequestHeader(value = "Reference-Id", required = false) String headerReferenceId
    ) {
        String referenceId = headerReferenceId != null ? headerReferenceId
                : request.referenceId() != null ? request.referenceId() : UUID.randomUUID().toString();

        return accountService.transact(
                accountId,
                new TransactionRequest(request.type(), request.amount(), referenceId)
        );
    }

    @PreAuthorize("hasAnyRole('VIEWER','OPERATOR')")
    @PostMapping("/ledger/search")
    public List<LedgerEntryView> searchLedger(@RequestBody(required = false) LedgerSearchRequest request) {
        LedgerSearchRequest condition = request == null
                ? new LedgerSearchRequest(null, null, null, null, null, null, null, 100)
                : request;
        return accountService.searchLedger(condition);
    }
}
