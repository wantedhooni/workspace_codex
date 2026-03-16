package com.example.marketsignal.report;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 일간 리포트 조회 API를 제공한다.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Validated
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/today")
    public DailyReportResponse getToday() {
        return reportService.getTodayReport();
    }

    @GetMapping("/recent")
    public List<DailyReportResponse> getRecent(
            @RequestParam(defaultValue = "5")
            @Min(value = 1, message = "조회 개수는 1 이상이어야 합니다.")
            @Max(value = 10, message = "조회 개수는 10 이하여야 합니다.")
            int limit
    ) {
        return reportService.getRecentReports(limit);
    }
}
