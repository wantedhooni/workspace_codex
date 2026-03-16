package com.example.marketsignal.report;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.marketsignal.auth.JwtAuthenticationFilter;
import com.example.marketsignal.macro.MarketRegime;
import com.example.marketsignal.signal.SignalAction;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("최근 리포트 조회 API는 리스트를 반환한다")
    void getRecentReturnsReports() throws Exception {
        when(reportService.getRecentReports(anyInt())).thenReturn(List.of(
                new DailyReportResponse(
                        LocalDate.of(2026, 3, 16),
                        LocalDate.of(2026, 3, 13),
                        java.time.LocalDateTime.of(2026, 3, 16, 8, 40),
                        MarketRegime.GROWTH,
                        List.of("SOFTWARE", "SEMICONDUCTOR"),
                        List.of(new SignalSummaryResponse("NVDA", 6, SignalAction.BUY, List.of("20DMA 상회", "상대 강도 우위"))),
                        "성장주가 주도권을 유지하고 있습니다."
                )
        ));

        mockMvc.perform(get("/api/reports/recent").param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reportDate").value("2026-03-16"))
                .andExpect(jsonPath("$[0].snapshotDate").value("2026-03-13"))
                .andExpect(jsonPath("$[0].marketRegime").value("GROWTH"))
                .andExpect(jsonPath("$[0].topSignals[0].ticker").value("NVDA"))
                .andExpect(jsonPath("$[0].topSignals[0].reasons[0]").value("20DMA 상회"));
    }
}
