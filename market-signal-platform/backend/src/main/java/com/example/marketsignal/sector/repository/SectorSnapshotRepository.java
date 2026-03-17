package com.example.marketsignal.sector;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 섹터 스냅샷 저장소 접근을 담당한다.
 */
public interface SectorSnapshotRepository extends JpaRepository<SectorSnapshot, Long> {

    List<SectorSnapshot> findAllBySnapshotDateOrderByStrengthScoreDesc(LocalDate snapshotDate);

    boolean existsBySnapshotDate(LocalDate snapshotDate);
}
