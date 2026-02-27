package com.quant.portal.api.presentation.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QuantMacroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldManageQuantAlgorithmAndMacroResources() throws Exception {
        MvcResult instrumentResult = mockMvc.perform(post("/api/v1/instruments")
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ticker": "MSFT_QA",
                                  "name": "Microsoft",
                                  "marketCode": "US",
                                  "currencyCode": "USD"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ticker").value("MSFT_QA"))
                .andReturn();

        JsonNode instrumentNode = objectMapper.readTree(instrumentResult.getResponse().getContentAsString());
        long instrumentId = instrumentNode.path("data").path("id").asLong();

        MvcResult strategyResult = mockMvc.perform(post("/api/v1/quant-strategies")
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "US Quality Momentum",
                                  "style": "MOMENTUM",
                                  "status": "ACTIVE",
                                  "rebalanceCycleDays": 20,
                                  "description": "Monthly quality momentum basket"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("US Quality Momentum"))
                .andExpect(jsonPath("$.data.style").value("MOMENTUM"))
                .andReturn();

        JsonNode strategyNode = objectMapper.readTree(strategyResult.getResponse().getContentAsString());
        long strategyId = strategyNode.path("data").path("id").asLong();

        MvcResult signalResult = mockMvc.perform(post("/api/v1/quant-signals")
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "strategyId": %d,
                                  "instrumentId": %d,
                                  "signalType": "BUY",
                                  "signalDate": "2026-02-13",
                                  "score": 0.82,
                                  "confidence": 0.74,
                                  "rationale": "Earnings revision and trend persistence"
                                }
                                """.formatted(strategyId, instrumentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.signalType").value("BUY"))
                .andExpect(jsonPath("$.data.strategyId").value(strategyId))
                .andExpect(jsonPath("$.data.instrumentId").value(instrumentId))
                .andReturn();

        JsonNode signalNode = objectMapper.readTree(signalResult.getResponse().getContentAsString());
        long signalId = signalNode.path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/quant-signals/{id}", signalId)
                        .with(httpBasic("user", "user1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.strategyName").value("US Quality Momentum"))
                .andExpect(jsonPath("$.data.ticker").value("MSFT_QA"));

        MvcResult macroResult = mockMvc.perform(post("/api/v1/macro-indicators")
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "indicatorCode": "US_CPI_YOY",
                                  "indicatorName": "US CPI YoY",
                                  "regionCode": "US",
                                  "observedDate": "2026-02-01",
                                  "indicatorValue": 2.9,
                                  "unit": "%",
                                  "source": "BLS"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.indicatorCode").value("US_CPI_YOY"))
                .andReturn();

        JsonNode macroNode = objectMapper.readTree(macroResult.getResponse().getContentAsString());
        long macroId = macroNode.path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/quant-strategies")
                        .with(httpBasic("user", "user1234"))
                        .param("filter", "{\"keyword\":\"Momentum\",\"status\":\"ACTIVE\"}")
                        .param("range", "[0,24]")
                        .param("sort", "[\"createdAt\",\"DESC\"]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.data[0].id").value(strategyId));

        mockMvc.perform(get("/api/v1/quant-signals")
                        .with(httpBasic("user", "user1234"))
                        .param("filter", "{\"strategyId\":%d,\"signalType\":\"BUY\"}".formatted(strategyId))
                        .param("page", "1")
                        .param("perPage", "25")
                        .param("sort", "signalDate,DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.data[0].id").value(signalId));

        mockMvc.perform(get("/api/v1/macro-indicators")
                        .with(httpBasic("user", "user1234"))
                        .param("filter", "{\"regionCode\":\"US\",\"fromDate\":\"2026-01-01\",\"toDate\":\"2026-12-31\"}")
                        .param("range", "[0,24]")
                        .param("sort", "[\"observedDate\",\"DESC\"]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.data[0].id").value(macroId));

        mockMvc.perform(put("/api/v1/macro-indicators/{id}", macroId)
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "indicatorName": "US CPI YoY Updated",
                                  "regionCode": "US",
                                  "observedDate": "2026-02-01",
                                  "indicatorValue": 2.8,
                                  "unit": "%",
                                  "source": "BLS"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.indicatorName").value("US CPI YoY Updated"))
                .andExpect(jsonPath("$.data.indicatorValue").value(2.8));

        mockMvc.perform(get("/api/v1/quant-signals")
                        .with(httpBasic("user", "user1234"))
                        .param("page", "1")
                        .param("perPage", "25")
                        .param("sort", "unsupported,DESC"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));

        mockMvc.perform(delete("/api/v1/quant-strategies/{id}", strategyId)
                        .with(httpBasic("user", "user1234")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("CONFLICT"));

        mockMvc.perform(delete("/api/v1/quant-signals/{id}", signalId)
                        .with(httpBasic("user", "user1234")))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/v1/quant-strategies/{id}", strategyId)
                        .with(httpBasic("user", "user1234")))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/v1/macro-indicators/{id}", macroId)
                        .with(httpBasic("user", "user1234")))
                .andExpect(status().isNoContent());
    }
}
