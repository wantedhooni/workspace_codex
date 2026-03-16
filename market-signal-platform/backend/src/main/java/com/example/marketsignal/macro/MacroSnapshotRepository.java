package com.example.marketsignal.macro;

import java.util.Optional;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 매크로 스냅샷 저장소 접근을 담당한다.
 */
public interface MacroSnapshotRepository extends JpaRepository<MacroSnapshot, Long> {

    Optional<MacroSnapshot> findTopByOrderBySnapshotDateDesc();

    boolean existsBySnapshotDate(LocalDate snapshotDate);
}
