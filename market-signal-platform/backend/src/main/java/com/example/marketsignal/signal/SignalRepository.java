package com.example.marketsignal.signal;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 시그널 저장소 접근을 담당한다.
 */
public interface SignalRepository extends JpaRepository<Signal, Long> {

    List<Signal> findAllBySnapshotDateOrderByScoreDesc(LocalDate snapshotDate);

    void deleteAllBySnapshotDate(LocalDate snapshotDate);
}
