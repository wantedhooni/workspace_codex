package com.example.marketsignal.news;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.marketsignal.auth.JwtAuthenticationFilter;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NewsController.class)
@AutoConfigureMockMvc(addFilters = false)
class NewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NewsAnalysisService newsAnalysisService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("최근 뉴스 분석 조회 API는 최신 이력을 반환한다")
    void getRecentAnalysesReturnsHistory() throws Exception {
        when(newsAnalysisService.getRecentAnalyses(anyInt(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(List.of(
                new NewsAnalysisResponse(
                        10L,
                        "NVIDIA surges after strong guidance",
                        NewsSentiment.POSITIVE,
                        "강한 가이던스가 센티먼트를 개선했습니다.",
                        "HIGH",
                        "실적 모멘텀이 반도체 섹터 전반으로 확산될 수 있습니다.",
                        LocalDateTime.of(2026, 3, 16, 8, 30)
                )
        ));

        mockMvc.perform(get("/api/news/analyses")
                        .param("limit", "4")
                        .param("query", "nvidia")
                        .param("sentiment", "POSITIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].headline").value("NVIDIA surges after strong guidance"))
                .andExpect(jsonPath("$[0].impact").value("HIGH"));
    }
}
