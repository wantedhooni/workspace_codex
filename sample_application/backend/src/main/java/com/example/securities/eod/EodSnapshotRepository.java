package com.example.securities.eod;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EodSnapshotRepository extends JpaRepository<EodSnapshot, String> {

    Optional<EodSnapshot> findByBusinessDate(LocalDate businessDate);
}
