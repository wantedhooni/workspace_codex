package com.derivops.mvp.account.api;
import com.derivops.mvp.account.*;
import com.derivops.mvp.account.application.*;
import com.derivops.mvp.account.dto.*;
import com.derivops.mvp.account.infrastructure.*;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    @PreAuthorize("hasAnyRole('OPS_ADMIN','OPS_VIEWER','AUDITOR')")
    @GetMapping
    public Page<AccountListItemResponse> list(
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(required = false) String broker,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean unmask
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return accountService.getAccounts(status, broker, keyword, filter, pageable, unmask);
    }

    @PreAuthorize("hasAnyRole('OPS_ADMIN','OPS_VIEWER','AUDITOR')")
    @GetMapping("/{accountId}/summary")
    public AccountSummaryResponse summary(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "false") boolean unmask
    ) {
        return accountService.getSummary(accountId, unmask);
    }
}
