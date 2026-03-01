package com.derivops.mvp.ledger.infrastructure;

import com.derivops.mvp.ledger.LedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LedgerEntryRepositoryCustom {
    Page<LedgerEntry> search(Long accountId, String referenceId, String filter, Pageable pageable);
}
