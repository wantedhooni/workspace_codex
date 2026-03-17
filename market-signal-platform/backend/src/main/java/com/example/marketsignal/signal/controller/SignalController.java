package com.example.marketsignal.signal;

import com.example.marketsignal.report.DailyReportResponse;
import com.example.marketsignal.report.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 종목 시그널 생성 API를 제공한다.
 */
@RestController
@RequestMapping("/api/signals")
@RequiredArgsConstructor
public class SignalController {

    private final ReportService reportService;

    @PostMapping("/generate")
    public DailyReportResponse generate() {
        return reportService.generateTodayReport();
    }
}
