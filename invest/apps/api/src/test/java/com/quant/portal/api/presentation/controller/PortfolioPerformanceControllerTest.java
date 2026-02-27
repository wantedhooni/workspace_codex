package com.quant.portal.api.presentation.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quant.portal.api.application.service.StooqEodPriceClient;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.market-data.stooq-enabled=true")
class PortfolioPerformanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StooqEodPriceClient stooqEodPriceClient;

    @Test
    void shouldReturnPortfolioEodPerformanceFromDailyClose() throws Exception {
        when(stooqEodPriceClient.fetchLatestQuote(eq("AAPL")))
                .thenReturn(Optional.of(new StooqEodPriceClient.StooqEodQuote(
                        LocalDate.parse("2026-02-13"),
                        new BigDecimal("1200")
                )));

        MvcResult portfolioResult = mockMvc.perform(post("/api/v1/portfolios")
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Performance Portfolio",
                                  "baseCurrency": "KRW"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        Long portfolioId = objectMapper.readTree(portfolioResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();

        MvcResult instrumentResult = mockMvc.perform(post("/api/v1/instruments")
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ticker": "AAPL",
                                  "name": "Apple",
                                  "marketCode": "US",
                                  "currencyCode": "USD"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        Long instrumentId = objectMapper.readTree(instrumentResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();

        mockMvc.perform(post("/api/v1/transactions")
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": %d,
                                  "transactionType": "DEPOSIT",
                                  "tradeDate": "2026-02-11",
                                  "amount": 10000,
                                  "currencyCode": "KRW"
                                }
                                """.formatted(portfolioId)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/transactions")
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": %d,
                                  "instrumentId": %d,
                                  "transactionType": "BUY",
                                  "tradeDate": "2026-02-11",
                                  "quantity": 2,
                                  "unitPrice": 1000,
                                  "fee": 10,
                                  "tax": 0,
                                  "currencyCode": "KRW"
                                }
                                """.formatted(portfolioId, instrumentId)))
                .andExpect(status().isOk());

        MvcResult getResult = mockMvc.perform(get("/api/v1/portfolios/{id}", portfolioId)
                        .with(httpBasic("user", "user1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cashBalance").value(7990))
                .andExpect(jsonPath("$.data.investedAmount").value(2010))
                .andExpect(jsonPath("$.data.marketValue").value(2400))
                .andExpect(jsonPath("$.data.eodValuationAmount").value(10390))
                .andExpect(jsonPath("$.data.eodProfitLoss").value(390))
                .andExpect(jsonPath("$.data.eodReturnRate").value(3.9))
                .andExpect(jsonPath("$.data.eodPriceDate").value("2026-02-13"))
                .andExpect(jsonPath("$.data.pricedHoldings").value(1))
                .andExpect(jsonPath("$.data.totalHoldings").value(1))
                .andReturn();

        JsonNode node = objectMapper.readTree(getResult.getResponse().getContentAsString());
        long id = node.path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/portfolios")
                        .with(httpBasic("user", "user1234"))
                        .param("filter", "{\"keyword\":\"Performance Portfolio\"}")
                        .param("page", "1")
                        .param("perPage", "25")
                        .param("sort", "createdAt,DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.data[0].id").value(id))
                .andExpect(jsonPath("$.data[0].eodReturnRate").value(3.9));
    }
}
