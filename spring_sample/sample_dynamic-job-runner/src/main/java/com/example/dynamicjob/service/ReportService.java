package com.example.dynamicjob.service;

import com.example.dynamicjob.dto.ReportRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("reportService")
public class ReportService {
    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    public void generateDailyReport(ReportRequest request) {
        log.info("generateDailyReport called. request={}", request);
    }

    public void rebuildMonthlySummary(String month) {
        log.info("rebuildMonthlySummary called. month={}", month);
    }
}
