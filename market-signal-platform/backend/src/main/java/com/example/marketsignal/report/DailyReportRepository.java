package com.example.marketsignal.report;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 일간 리포트 저장소 접근을 담당한다.
 */
public interface DailyReportRepository extends JpaRepository<DailyReport, Long> {

    @EntityGraph(attributePaths = "signals")
    Optional<DailyReport> findByReportDate(LocalDate reportDate);

    @EntityGraph(attributePaths = "signals")
    List<DailyReport> findAllByOrderByReportDateDesc(Pageable pageable);
}
