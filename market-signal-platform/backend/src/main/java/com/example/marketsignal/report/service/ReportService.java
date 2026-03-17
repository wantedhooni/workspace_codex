package com.example.marketsignal.report;

import com.example.marketsignal.ai.AiNarrativeService;
import com.example.marketsignal.common.BusinessException;
import com.example.marketsignal.macro.MacroSnapshot;
import com.example.marketsignal.macro.MacroSnapshotRepository;
import com.example.marketsignal.macro.MarketRegime;
import com.example.marketsignal.macro.MarketRegimeEngine;
import com.example.marketsignal.sector.SectorStrengthService;
import com.example.marketsignal.signal.Signal;
import com.example.marketsignal.signal.SignalRepository;
import com.example.marketsignal.signal.SignalResult;
import com.example.marketsignal.signal.SignalScoringService;
import com.example.marketsignal.stock.StockSnapshotRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 일간 리포트 생성과 조회를 담당한다.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private static final String TODAY_REPORT_CACHE = "today-report-v2";

    private final MacroSnapshotRepository macroSnapshotRepository;
    private final StockSnapshotRepository stockSnapshotRepository;
    private final SectorStrengthService sectorStrengthService;
    private final MarketRegimeEngine marketRegimeEngine;
    private final SignalScoringService signalScoringService;
    private final DailyReportRepository dailyReportRepository;
    private final SignalRepository signalRepository;
    private final AiNarrativeService aiNarrativeService;

    /**
     * 오늘의 리포트를 새로 생성한다.
     */
    @Transactional
    @CacheEvict(cacheNames = TODAY_REPORT_CACHE, key = "'today'")
    public DailyReportResponse generateTodayReport() {
        MacroSnapshot macroSnapshot = macroSnapshotRepository.findTopByOrderBySnapshotDateDesc()
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "매크로 데이터가 없습니다."));

        LocalDate reportDate = LocalDate.now();
        MarketRegime marketRegime = marketRegimeEngine.determine(macroSnapshot);
        List<String> leadingSectors = sectorStrengthService.getLeadingSectors(macroSnapshot.getSnapshotDate(), 2);

        List<Signal> signals = stockSnapshotRepository.findAllBySnapshotDate(macroSnapshot.getSnapshotDate()).stream()
                .map(snapshot -> {
                    SignalResult result = signalScoringService.scoreStock(snapshot);
                    return Signal.builder()
                            .snapshotDate(macroSnapshot.getSnapshotDate())
                            .ticker(result.ticker())
                            .score(result.score())
                            .action(result.action())
                            .reasons(result.reasons())
                            .build();
                })
                .sorted((left, right) -> Integer.compare(right.getScore(), left.getScore()))
                .limit(5)
                .toList();

        String summary = aiNarrativeService.generateReportSummary(marketRegime, leadingSectors);
        DailyReport report = dailyReportRepository.findByReportDate(reportDate)
                .orElseGet(() -> DailyReport.builder()
                        .reportDate(reportDate)
                        .snapshotDate(macroSnapshot.getSnapshotDate())
                        .marketRegime(marketRegime)
                        .leadingSectors(leadingSectors)
                        .summary(summary)
                        .build());

        report.refresh(macroSnapshot.getSnapshotDate(), marketRegime, leadingSectors, summary);
        signalRepository.deleteAllBySnapshotDate(macroSnapshot.getSnapshotDate());
        report.replaceSignals(signals);
        DailyReport saved = dailyReportRepository.save(report);
        return DailyReportResponse.from(saved);
    }

    /**
     * 오늘 생성된 리포트를 조회한다.
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = TODAY_REPORT_CACHE, key = "'today'")
    public DailyReportResponse getTodayReport() {
        DailyReport report = dailyReportRepository.findByReportDate(LocalDate.now())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "오늘 생성된 리포트가 없습니다."));
        return DailyReportResponse.from(report);
    }

    /**
     * 최근 생성된 리포트 목록을 조회한다.
     */
    @Transactional(readOnly = true)
    public List<DailyReportResponse> getRecentReports(int limit) {
        int normalizedLimit = Math.min(Math.max(limit, 1), 10);
        return dailyReportRepository.findAllByOrderByReportDateDesc(PageRequest.of(0, normalizedLimit)).stream()
                .map(DailyReportResponse::from)
                .toList();
    }
}
