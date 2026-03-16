package com.example.marketsignal.stock;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 종목 스냅샷 저장소 접근을 담당한다.
 */
public interface StockSnapshotRepository extends JpaRepository<StockSnapshot, Long> {

    List<StockSnapshot> findAllBySnapshotDate(LocalDate snapshotDate);

    List<StockSnapshot> findAllBySnapshotDateAndTickerIn(LocalDate snapshotDate, Collection<String> tickers);

    Optional<StockSnapshot> findTopByOrderBySnapshotDateDesc();

    boolean existsBySnapshotDate(LocalDate snapshotDate);
}
