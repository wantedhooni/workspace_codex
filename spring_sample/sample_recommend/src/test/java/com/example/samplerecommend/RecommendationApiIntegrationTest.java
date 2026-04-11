package com.example.samplerecommend;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.samplerecommend.domain.RecommendationRequestLog;
import com.example.samplerecommend.repository.RecommendationRequestLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 추천 API와 행동 적재 API의 통합 흐름을 검증한다.
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class RecommendationApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RecommendationRequestLogRepository recommendationRequestLogRepository;

    @Test
    @DisplayName("고객 추천 조회 시 추천 결과와 로그가 함께 저장된다.")
    void recommendProducts() throws Exception {
        mockMvc.perform(get("/api/recommendations")
                        .param("customerId", "CUST-001")
                        .param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("CUST-001"))
                .andExpect(jsonPath("$.items.length()").value(3))
                .andExpect(jsonPath("$.items[0].productCode").value("P-1002"));

        RecommendationRequestLog latestLog = recommendationRequestLogRepository.findTop20ByOrderByRequestedAtDesc().getFirst();
        assertThat(latestLog.getCustomerId()).isEqualTo("CUST-001");
        assertThat(latestLog.getRecommendedProductCodes()).contains("P-1002");
    }

    @Test
    @DisplayName("고객 행동 적재 후 추천 결과에 해당 상품 관심도가 반영된다.")
    void recordActionAndReflectRecommendation() throws Exception {
        mockMvc.perform(post("/api/customers/CUST-003/actions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productCode": "P-3001",
                                  "actionType": "PURCHASE",
                                  "weight": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("CUST-003"))
                .andExpect(jsonPath("$.productCode").value("P-3001"));

        mockMvc.perform(get("/api/recommendations")
                        .param("customerId", "CUST-003")
                        .param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productCode").value("P-3001"));
    }
}

