package com.revy.mvpbanking.customer.presentation;

import com.revy.mvpbanking.audit.application.AuditLogService;
import com.revy.mvpbanking.audit.domain.AuditActionType;
import com.revy.mvpbanking.common.api.ApiResponse;
import com.revy.mvpbanking.common.api.PageResponse;
import com.revy.mvpbanking.customer.application.CustomerQueryService;
import com.revy.mvpbanking.customer.domain.CustomerStatus;
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
@RequestMapping("/api/admin/customers")
public class AdminCustomerController {

    private final CustomerQueryService customerQueryService;
    private final AuditLogService auditLogService;

    public AdminCustomerController(CustomerQueryService customerQueryService, AuditLogService auditLogService) {
        this.customerQueryService = customerQueryService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ApiResponse<PageResponse<CustomerSummaryResponse>> list(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(required = false) CustomerStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        auditLogService.logCurrentActor(AuditActionType.CUSTOMER_LIST_VIEWED, "CUSTOMER", "all", "Viewed customer list");
        return ApiResponse.ok(PageResponse.from(
                customerQueryService.getCustomers(
                                query,
                                status,
                                toStartOfDay(createdFrom),
                                toExclusiveEndOfDay(createdTo),
                                sortBy,
                                sortDir,
                                page,
                                size
                        )
                        .map(CustomerSummaryResponse::from)
        ));
    }

    @GetMapping("/summary")
    public ApiResponse<CustomerStatusSummaryResponse> summary(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo
    ) {
        return ApiResponse.ok(CustomerStatusSummaryResponse.from(
                customerQueryService.getCustomerSummary(query, toStartOfDay(createdFrom), toExclusiveEndOfDay(createdTo))
        ));
    }

    private Instant toStartOfDay(LocalDate date) {
        return date == null ? null : date.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    private Instant toExclusiveEndOfDay(LocalDate date) {
        return date == null ? null : date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
    }
}
