package com.derivops.mvp.journalentry.infrastructure;

import com.derivops.mvp.journalentry.JournalEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JournalEntryRepositoryCustom {
    Page<JournalEntry> search(Long accountId, String journalNo, String referenceId, String filter, Pageable pageable);
}
