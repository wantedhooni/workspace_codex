package com.derivops.mvp.ledger.application;

import com.derivops.mvp.ledger.LedgerEntry;
import com.derivops.mvp.ledger.dto.LedgerEntryResponse;
import com.derivops.mvp.ledger.infrastructure.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class LedgerEntryService {

    private final LedgerEntryRepository ledgerEntryRepository;

    @Transactional(readOnly = true)
    public Page<LedgerEntryResponse> list(Long accountId, String referenceId, String filter, Pageable pageable) {
        return ledgerEntryRepository.search(accountId, referenceId, filter, pageable).map(this::toResponse);
    }

    private LedgerEntryResponse toResponse(LedgerEntry item) {
        return new LedgerEntryResponse(
                item.getId(),
                item.getEntryNo(),
                item.getAccount().getId(),
                item.getAccount().getAccountNo(),
                item.getReferenceType(),
                item.getReferenceId(),
                item.getSymbol(),
                item.getPostingDate(),
                item.getCurrency(),
                item.getQuantityChange(),
                item.getAmountChange(),
                item.getRunningQuantity(),
                item.getRunningAmount(),
                item.getDescription(),
                item.getCreatedAt()
        );
    }
}
