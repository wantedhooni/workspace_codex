package com.derivops.mvp.ledger.api;

import com.derivops.mvp.ledger.application.LedgerEntryService;
import com.derivops.mvp.ledger.dto.LedgerEntryResponse;
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
@RequestMapping("/api/v1/ledger-entries")
public class LedgerEntryController {

    private final LedgerEntryService ledgerEntryService;

    @PreAuthorize("hasAnyRole('OPS_ADMIN','OPS_VIEWER','AUDITOR')")
    @GetMapping
    public Page<LedgerEntryResponse> list(
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) String referenceId,
            @RequestParam(required = false) String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        return ledgerEntryService.list(accountId, referenceId, filter, PageRequest.of(page, size));
    }
}
