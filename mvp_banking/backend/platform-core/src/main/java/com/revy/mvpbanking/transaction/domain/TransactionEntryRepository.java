package com.revy.mvpbanking.transaction.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionEntryRepository extends JpaRepository<TransactionEntry, UUID> {
    List<TransactionEntry> findAllByOrderByOccurredAtDesc();
    List<TransactionEntry> findByAccountIdInOrderByOccurredAtDesc(List<UUID> accountIds);
    Optional<TransactionEntry> findByTransactionNumber(String transactionNumber);
}
