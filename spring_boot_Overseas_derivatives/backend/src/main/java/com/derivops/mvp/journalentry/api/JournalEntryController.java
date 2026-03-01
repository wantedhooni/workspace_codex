package com.derivops.mvp.journalentry.api;

import com.derivops.mvp.journalentry.application.JournalEntryService;
import com.derivops.mvp.journalentry.dto.JournalEntryResponse;
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
@RequestMapping("/api/v1/journal-entries")
public class JournalEntryController {

    private final JournalEntryService journalEntryService;

    @PreAuthorize("hasAnyRole('OPS_ADMIN','OPS_VIEWER','AUDITOR')")
    @GetMapping
    public Page<JournalEntryResponse> list(
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) String journalNo,
            @RequestParam(required = false) String referenceId,
            @RequestParam(required = false) String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        return journalEntryService.list(accountId, journalNo, referenceId, filter, PageRequest.of(page, size));
    }
}
