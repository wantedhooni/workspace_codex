package com.example.securities.account;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, String> {

    Optional<LedgerEntry> findByReferenceId(String referenceId);
}
