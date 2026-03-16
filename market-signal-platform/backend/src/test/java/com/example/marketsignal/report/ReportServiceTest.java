package com.example.marketsignal.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.marketsignal.ai.AiNarrativeService;
import com.example.marketsignal.macro.MacroSnapshot;
import com.example.marketsignal.macro.MacroSnapshotRepository;
import com.example.marketsignal.macro.MarketRegime;
import com.example.marketsignal.macro.MarketRegimeEngine;
import com.example.marketsignal.sector.SectorStrengthService;
import com.example.marketsignal.signal.SignalRepository;
import com.example.marketsignal.signal.SignalScoringService;
import com.example.marketsignal.signal.SignalResult;
import com.example.marketsignal.stock.StockSnapshot;
import com.example.marketsignal.stock.StockSnapshotRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private MacroSnapshotRepository macroSnapshotRepository;

    @Mock
    private StockSnapshotRepository stockSnapshotRepository;

    @Mock
    private SectorStrengthService sectorStrengthService;

    @Mock
    private MarketRegimeEngine marketRegimeEngine;

    @Mock
    private SignalScoringService signalScoringService;

    @Mock
    private DailyReportRepository dailyReportRepository;

    @Mock
    private SignalRepository signalRepository;

    @Mock
    private AiNarrativeService aiNarrativeService;

    @InjectMocks
    private ReportService reportService;

    @Test
    @DisplayName("오늘 리포트 생성 시 실제 시장 스냅샷 기준일로 시그널을 교체한다")
    void generateTodayReportUsesSnapshotDateForSignals() {
        LocalDate snapshotDate = LocalDate.of(2026, 3, 13);
        MacroSnapshot macroSnapshot = MacroSnapshot.builder()
                .snapshotDate(snapshotDate)
                .tenYearYieldChange(-0.4)
                .dxyChange(-0.3)
                .oilChange(0.8)
                .futuresChange(0.2)
                .build();
        StockSnapshot stockSnapshot = StockSnapshot.builder()
                .snapshotDate(snapshotDate)
                .ticker("NVDA")
                .companyName("NVIDIA")
                .above20Dma(true)
                .above50Dma(true)
                .relativeStrengthStrong(true)
                .earningsReactionPositive(false)
                .volumeSurge(true)
                .negativeNewsWeakPrice(false)
                .build();
        SignalResult signalResult = new SignalResult("NVDA", 4, com.example.marketsignal.signal.SignalAction.WATCH, "20DMA 상회");

        when(macroSnapshotRepository.findTopByOrderBySnapshotDateDesc()).thenReturn(Optional.of(macroSnapshot));
        when(stockSnapshotRepository.findAllBySnapshotDate(snapshotDate)).thenReturn(List.of(stockSnapshot));
        when(marketRegimeEngine.determine(macroSnapshot)).thenReturn(MarketRegime.GROWTH);
        when(sectorStrengthService.getLeadingSectors(snapshotDate, 2)).thenReturn(List.of("SOFTWARE", "SEMICONDUCTOR"));
        when(signalScoringService.scoreStock(stockSnapshot)).thenReturn(signalResult);
        when(aiNarrativeService.generateReportSummary(MarketRegime.GROWTH, List.of("SOFTWARE", "SEMICONDUCTOR")))
                .thenReturn("성장 섹터 우위가 이어지고 있습니다.");
        when(dailyReportRepository.findByReportDate(LocalDate.now())).thenReturn(Optional.empty());
        when(dailyReportRepository.save(any(DailyReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DailyReportResponse response = reportService.generateTodayReport();

        verify(signalRepository).deleteAllBySnapshotDate(snapshotDate);
        assertThat(response.snapshotDate()).isEqualTo(snapshotDate);
        assertThat(response.topSignals()).hasSize(1);
        assertThat(response.topSignals().get(0).ticker()).isEqualTo("NVDA");
    }
}
