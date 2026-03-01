package com.derivops.mvp.journalentry.application;

import com.derivops.mvp.journalentry.JournalEntry;
import com.derivops.mvp.journalentry.dto.JournalEntryResponse;
import com.derivops.mvp.journalentry.infrastructure.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;

    @Transactional(readOnly = true)
    public Page<JournalEntryResponse> list(Long accountId, String journalNo, String referenceId, String filter, Pageable pageable) {
        return journalEntryRepository.search(accountId, journalNo, referenceId, filter, pageable).map(this::toResponse);
    }

    private JournalEntryResponse toResponse(JournalEntry item) {
        return new JournalEntryResponse(
                item.getId(),
                item.getJournalNo(),
                item.getLineNo(),
                item.getAccount().getId(),
                item.getAccount().getAccountNo(),
                item.getReferenceType(),
                item.getReferenceId(),
                item.getPostingDate(),
                item.getAccountCode(),
                item.getDebitAmount(),
                item.getCreditAmount(),
                item.getCurrency(),
                item.getDescription(),
                item.getCreatedAt()
        );
    }
}
