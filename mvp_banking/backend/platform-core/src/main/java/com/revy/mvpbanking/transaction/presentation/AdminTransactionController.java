package com.revy.mvpbanking.transaction.presentation;

import com.revy.mvpbanking.audit.application.AuditLogService;
import com.revy.mvpbanking.audit.domain.AuditActionType;
import com.revy.mvpbanking.common.api.ApiResponse;
import com.revy.mvpbanking.common.api.PageResponse;
import com.revy.mvpbanking.transaction.application.TransactionQueryService;
import com.revy.mvpbanking.transaction.domain.TransactionStatus;
import com.revy.mvpbanking.transaction.domain.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("admin-api")
@RequestMapping("/api/admin/transactions")
public class AdminTransactionController {

    private final TransactionQueryService transactionQueryService;
    private final AuditLogService auditLogService;

    public AdminTransactionController(
            TransactionQueryService transactionQueryService,
            AuditLogService auditLogService
    ) {
        this.transactionQueryService = transactionQueryService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ApiResponse<PageResponse<TransactionSummaryResponse>> list(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) TransactionType transactionType,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate occurredFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate occurredTo,
            @RequestParam(defaultValue = "occurredAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        auditLogService.logCurrentActor(AuditActionType.TRANSACTION_LIST_VIEWED, "TRANSACTION", "all", "Viewed transaction list");
        return ApiResponse.ok(PageResponse.from(
                transactionQueryService.getAllTransactions(
                                query,
                                status,
                                transactionType,
                                minAmount,
                                maxAmount,
                                toStartOfDay(occurredFrom),
                                toExclusiveEndOfDay(occurredTo),
                                sortBy,
                                sortDir,
                                page,
                                size
                        )
                        .map(TransactionSummaryResponse::from)
        ));
    }

    private Instant toStartOfDay(LocalDate date) {
        return date == null ? null : date.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    private Instant toExclusiveEndOfDay(LocalDate date) {
        return date == null ? null : date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
    }
}
