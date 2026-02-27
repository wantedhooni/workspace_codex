package com.derivops.mvp.audit;

import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditController {

    private final AuditLogService auditLogService;

    @PreAuthorize("hasAnyRole('AUDITOR','OPS_ADMIN')")
    @GetMapping
    public Page<AuditLogResponse> list(
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return auditLogService.search(actor, action, keyword, filter, from, to, PageRequest.of(page, size));
    }
}
