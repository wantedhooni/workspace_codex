package com.example.marketsignal.report;

import com.example.marketsignal.macro.MarketRegime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 오늘의 리포트 응답 모델이다.
 */
public record DailyReportResponse(
        LocalDate reportDate,
        LocalDate snapshotDate,
        LocalDateTime generatedAt,
        MarketRegime marketRegime,
        List<String> leadingSectors,
        List<SignalSummaryResponse> topSignals,
        String summary
) {

    public static DailyReportResponse from(DailyReport report) {
        return new DailyReportResponse(
                report.getReportDate(),
                report.getSnapshotDate(),
                report.getUpdatedAt(),
                report.getMarketRegime(),
                report.getLeadingSectors(),
                report.getSignals().stream()
                        .sorted((left, right) -> Integer.compare(right.getScore(), left.getScore()))
                        .map(SignalSummaryResponse::from)
                        .toList(),
                report.getSummary()
        );
    }
}
