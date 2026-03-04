package com.revy.mvpbanking.account.presentation;

import com.revy.mvpbanking.account.application.AccountQueryService;
import com.revy.mvpbanking.account.domain.AccountStatus;
import com.revy.mvpbanking.account.domain.AccountType;
import com.revy.mvpbanking.audit.application.AuditLogService;
import com.revy.mvpbanking.audit.domain.AuditActionType;
import com.revy.mvpbanking.common.api.ApiResponse;
import com.revy.mvpbanking.common.api.PageResponse;
import java.math.BigDecimal;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("admin-api")
@RequestMapping("/api/admin/accounts")
public class AdminAccountController {

    private final AccountQueryService accountQueryService;
    private final AuditLogService auditLogService;

    public AdminAccountController(AccountQueryService accountQueryService, AuditLogService auditLogService) {
        this.accountQueryService = accountQueryService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ApiResponse<PageResponse<AccountSummaryResponse>> list(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(required = false) AccountType accountType,
            @RequestParam(required = false) BigDecimal minBalance,
            @RequestParam(required = false) BigDecimal maxBalance,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        auditLogService.logCurrentActor(AuditActionType.ACCOUNT_LIST_VIEWED, "ACCOUNT", "all", "Viewed account list");
        return ApiResponse.ok(PageResponse.from(
                accountQueryService.getAllAccounts(query, status, accountType, minBalance, maxBalance, sortBy, sortDir, page, size)
                        .map(AccountSummaryResponse::from)
        ));
    }
}
