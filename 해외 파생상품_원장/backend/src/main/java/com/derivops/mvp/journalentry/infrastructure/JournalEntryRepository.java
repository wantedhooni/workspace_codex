package com.derivops.mvp.journalentry.infrastructure;

import com.derivops.mvp.journalentry.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long>, JournalEntryRepositoryCustom {
}
