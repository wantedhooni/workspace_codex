package com.quant.portal.api.presentation.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
class TransactionFlowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreatePortfolioInstrumentAndTransactions() throws Exception {
        String primaryTicker = "AAPL_FLOW";
        String secondaryTicker = "TSLA_FLOW";

        MvcResult portfolioResult = mockMvc.perform(post("/api/v1/portfolios")
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Core",
                                  "baseCurrency": "KRW"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Core"))
                .andReturn();

        JsonNode portfolioNode = objectMapper.readTree(portfolioResult.getResponse().getContentAsString());
        Long portfolioId = portfolioNode.path("data").path("id").asLong();

        mockMvc.perform(put("/api/v1/portfolios/{id}", portfolioId)
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Core Updated"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Core Updated"));

        MvcResult instrumentResult = mockMvc.perform(post("/api/v1/instruments")
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ticker": "%s",
                                  "name": "Apple",
                                  "marketCode": "US",
                                  "currencyCode": "USD"
                                }
                                """.formatted(primaryTicker)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ticker").value(primaryTicker))
                .andReturn();

        JsonNode instrumentNode = objectMapper.readTree(instrumentResult.getResponse().getContentAsString());
        Long instrumentId = instrumentNode.path("data").path("id").asLong();

        mockMvc.perform(put("/api/v1/instruments/{id}", instrumentId)
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Apple Updated"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Apple Updated"));

        MvcResult depositResult = mockMvc.perform(post("/api/v1/transactions")
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": %d,
                                  "transactionType": "DEPOSIT",
                                  "tradeDate": "2026-02-11",
                                  "amount": 20000,
                                  "currencyCode": "KRW"
                                }
                                """.formatted(portfolioId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transactionType").value("DEPOSIT"))
                .andReturn();

        JsonNode depositNode = objectMapper.readTree(depositResult.getResponse().getContentAsString());
        Long depositTransactionId = depositNode.path("data").path("id").asLong();

        MvcResult buyResult = mockMvc.perform(post("/api/v1/transactions")
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": %d,
                                  "instrumentId": %d,
                                  "transactionType": "BUY",
                                  "tradeDate": "2026-02-11",
                                  "quantity": 3,
                                  "unitPrice": 1000,
                                  "fee": 10,
                                  "tax": 0,
                                  "currencyCode": "KRW"
                                }
                                """.formatted(portfolioId, instrumentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cashImpact").value(-3010))
                .andReturn();

        JsonNode buyNode = objectMapper.readTree(buyResult.getResponse().getContentAsString());
        Long buyTransactionId = buyNode.path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/transactions/{id}", buyTransactionId)
                        .with(httpBasic("user", "user1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(buyTransactionId))
                .andExpect(jsonPath("$.data.transactionType").value("BUY"));

        mockMvc.perform(put("/api/v1/transactions/{id}", depositTransactionId)
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tradeDate": "2026-02-11",
                                  "amount": 21000,
                                  "currencyCode": "KRW"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("CONFLICT"));

        mockMvc.perform(put("/api/v1/transactions/{id}", buyTransactionId)
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "instrumentId": %d,
                                  "tradeDate": "2026-02-11",
                                  "quantity": 2,
                                  "unitPrice": 900,
                                  "fee": 10,
                                  "tax": 0,
                                  "currencyCode": "KRW",
                                  "memo": "buy updated"
                                }
                                """.formatted(instrumentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cashImpact").value(-1810))
                .andExpect(jsonPath("$.data.memo").value("buy updated"));

        MvcResult holdingListResult = mockMvc.perform(get("/api/v1/holdings")
                        .with(httpBasic("user", "user1234"))
                        .param("portfolioId", String.valueOf(portfolioId))
                        .param("page", "1")
                        .param("perPage", "25")
                        .param("sort", "ticker,ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.data[0].ticker").value(primaryTicker))
                .andExpect(jsonPath("$.data[0].quantity").value(2))
                .andReturn();

        JsonNode holdingNode = objectMapper.readTree(holdingListResult.getResponse().getContentAsString());
        Long holdingId = holdingNode.path("data").path(0).path("id").asLong();

        MvcResult globalHoldingListResult = mockMvc.perform(get("/api/v1/holdings")
                        .with(httpBasic("user", "user1234"))
                        .param("page", "1")
                        .param("perPage", "25")
                        .param("sort", "ticker,ASC"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode globalHoldingListNode = objectMapper.readTree(globalHoldingListResult.getResponse().getContentAsString());
        boolean containsHolding = false;
        for (JsonNode item : globalHoldingListNode.path("data")) {
            if (item.path("id").asLong() == holdingId) {
                containsHolding = true;
                break;
            }
        }
        assertTrue(containsHolding, "Global holdings list should contain holding created in this flow");

        mockMvc.perform(get("/api/v1/holdings/{id}", holdingId)
                        .with(httpBasic("user", "user1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(holdingId))
                .andExpect(jsonPath("$.data.ticker").value(primaryTicker));

        mockMvc.perform(get("/api/v1/holdings")
                        .with(httpBasic("user", "user1234"))
                        .param("filter", "{\"portfolioId\":%d,\"keyword\":\"AAPL\"}".formatted(portfolioId))
                        .param("page", "1")
                        .param("perPage", "25")
                        .param("sort", "ticker,ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.data[0].ticker").value(primaryTicker));

        mockMvc.perform(get("/api/v1/holdings")
                        .with(httpBasic("user", "user1234"))
                        .param("filter", "{\"portfolioId\":%d}".formatted(portfolioId))
                        .param("range", "[0,24]")
                        .param("sort", "[\"ticker\",\"ASC\"]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.data[0].ticker").value(primaryTicker));

        mockMvc.perform(get("/api/v1/portfolios/{id}", portfolioId)
                        .with(httpBasic("user", "user1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cashBalance").value(18190));

        mockMvc.perform(get("/api/v1/instruments")
                        .with(httpBasic("user", "user1234"))
                        .param("keyword", "AAPL")
                        .param("marketCode", "US")
                        .param("page", "1")
                        .param("perPage", "25")
                        .param("sort", "ticker,ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.data[0].ticker").value(primaryTicker));

        mockMvc.perform(get("/api/v1/instruments")
                        .with(httpBasic("user", "user1234"))
                        .param("filter", "{\"keyword\":\"AAPL\",\"marketCode\":\"US\",\"currencyCode\":\"USD\"}")
                        .param("page", "1")
                        .param("perPage", "25")
                        .param("sort", "ticker,ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.data[0].ticker").value(primaryTicker));

        mockMvc.perform(get("/api/v1/instruments")
                        .with(httpBasic("user", "user1234"))
                        .param("filter", "{\"keyword\":\"AAPL\"}")
                        .param("range", "[0,24]")
                        .param("sort", "[\"ticker\",\"ASC\"]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.data[0].ticker").value(primaryTicker));

        mockMvc.perform(get("/api/v1/instruments")
                        .with(httpBasic("user", "user1234"))
                        .param("page", "1")
                        .param("perPage", "25")
                        .param("sort", "unsupported,DESC"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));

        mockMvc.perform(get("/api/v1/portfolios")
                        .with(httpBasic("user", "user1234"))
                        .param("keyword", "Updated")
                        .param("baseCurrency", "KRW")
                        .param("page", "1")
                        .param("perPage", "25")
                        .param("sort", "createdAt,DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Core Updated"));

        mockMvc.perform(get("/api/v1/portfolios")
                        .with(httpBasic("user", "user1234"))
                        .param("filter", "{\"keyword\":\"Updated\",\"baseCurrency\":\"KRW\"}")
                        .param("page", "1")
                        .param("perPage", "25")
                        .param("sort", "createdAt,DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Core Updated"));

        mockMvc.perform(get("/api/v1/portfolios")
                        .with(httpBasic("user", "user1234"))
                        .param("filter", "{\"keyword\":\"Updated\"}")
                        .param("range", "[0,24]")
                        .param("sort", "[\"createdAt\",\"DESC\"]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Core Updated"));

        mockMvc.perform(get("/api/v1/transactions")
                        .with(httpBasic("user", "user1234"))
                        .param("portfolioId", String.valueOf(portfolioId))
                        .param("transactionType", "BUY")
                        .param("page", "1")
                        .param("perPage", "25")
                        .param("sort", "tradeDate,DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.data[0].transactionType").value("BUY"));

        mockMvc.perform(get("/api/v1/transactions")
                        .with(httpBasic("user", "user1234"))
                        .param("filter", "{\"portfolioId\":%d,\"transactionType\":\"BUY\"}".formatted(portfolioId))
                        .param("page", "1")
                        .param("perPage", "25")
                        .param("sort", "tradeDate,DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.data[0].transactionType").value("BUY"));

        mockMvc.perform(get("/api/v1/transactions")
                        .with(httpBasic("user", "user1234"))
                        .param("filter", "{\"portfolioId\":%d,\"transactionType\":\"BUY\"}".formatted(portfolioId))
                        .param("range", "[0,24]")
                        .param("sort", "[\"tradeDate\",\"DESC\"]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.data[0].transactionType").value("BUY"));

        MvcResult globalTransactionListResult = mockMvc.perform(get("/api/v1/transactions")
                        .with(httpBasic("user", "user1234"))
                        .param("page", "1")
                        .param("perPage", "25")
                        .param("sort", "tradeDate,DESC"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode globalTransactionListNode = objectMapper.readTree(globalTransactionListResult.getResponse().getContentAsString());
        boolean containsDeposit = false;
        boolean containsBuy = false;
        for (JsonNode item : globalTransactionListNode.path("data")) {
            long transactionId = item.path("id").asLong();
            if (transactionId == depositTransactionId) {
                containsDeposit = true;
            }
            if (transactionId == buyTransactionId) {
                containsBuy = true;
            }
        }
        assertTrue(containsDeposit, "Global transactions list should contain deposit transaction created in this flow");
        assertTrue(containsBuy, "Global transactions list should contain buy transaction created in this flow");

        mockMvc.perform(get("/api/v1/instruments")
                        .with(httpBasic("user", "user1234"))
                        .param("filter", "{invalid-json}")
                        .param("page", "1")
                        .param("perPage", "25")
                        .param("sort", "ticker,ASC"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));

        mockMvc.perform(get("/api/v1/transactions")
                        .with(httpBasic("user", "user1234"))
                        .param("filter", "{\"portfolioId\":%d}".formatted(portfolioId))
                        .param("range", "[25,0]")
                        .param("sort", "[\"tradeDate\",\"DESC\"]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));

        mockMvc.perform(get("/api/v1/transactions")
                        .with(httpBasic("user", "user1234"))
                        .param("portfolioId", String.valueOf(portfolioId))
                        .param("page", "1")
                        .param("perPage", "25")
                        .param("sort", "unsupported,DESC"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));

        mockMvc.perform(get("/api/v1/holdings")
                        .with(httpBasic("user", "user1234"))
                        .param("portfolioId", String.valueOf(portfolioId))
                        .param("page", "1")
                        .param("perPage", "25")
                        .param("sort", "unsupported,DESC"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));

        mockMvc.perform(get("/api/v1/portfolios")
                        .with(httpBasic("user", "user1234"))
                        .param("page", "1")
                        .param("perPage", "25")
                        .param("sort", "unsupported,DESC"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));

        mockMvc.perform(delete("/api/v1/portfolios/{id}", portfolioId)
                        .with(httpBasic("user", "user1234")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("CONFLICT"));

        MvcResult emptyPortfolioResult = mockMvc.perform(post("/api/v1/portfolios")
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Empty Portfolio",
                                  "baseCurrency": "KRW"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode emptyPortfolioNode = objectMapper.readTree(emptyPortfolioResult.getResponse().getContentAsString());
        Long emptyPortfolioId = emptyPortfolioNode.path("data").path("id").asLong();

        mockMvc.perform(delete("/api/v1/portfolios/{id}", emptyPortfolioId)
                        .with(httpBasic("user", "user1234")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/portfolios/{id}", emptyPortfolioId)
                        .with(httpBasic("user", "user1234")))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/v1/instruments/{id}", instrumentId)
                        .with(httpBasic("user", "user1234")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("CONFLICT"));

        MvcResult emptyInstrumentResult = mockMvc.perform(post("/api/v1/instruments")
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ticker": "%s",
                                  "name": "Tesla",
                                  "marketCode": "US",
                                  "currencyCode": "USD"
                                }
                                """.formatted(secondaryTicker)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode emptyInstrumentNode = objectMapper.readTree(emptyInstrumentResult.getResponse().getContentAsString());
        Long emptyInstrumentId = emptyInstrumentNode.path("data").path("id").asLong();

        mockMvc.perform(delete("/api/v1/instruments/{id}", emptyInstrumentId)
                        .with(httpBasic("user", "user1234")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/instruments/{id}", emptyInstrumentId)
                        .with(httpBasic("user", "user1234")))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/v1/transactions/{id}", depositTransactionId)
                        .with(httpBasic("user", "user1234")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("CONFLICT"));

        mockMvc.perform(delete("/api/v1/transactions/{id}", buyTransactionId)
                        .with(httpBasic("user", "user1234")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/holdings")
                        .with(httpBasic("user", "user1234"))
                        .param("portfolioId", String.valueOf(portfolioId))
                        .param("page", "1")
                        .param("perPage", "25")
                        .param("sort", "ticker,ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));

        mockMvc.perform(delete("/api/v1/transactions/{id}", depositTransactionId)
                        .with(httpBasic("user", "user1234")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/transactions")
                        .with(httpBasic("user", "user1234"))
                        .param("portfolioId", String.valueOf(portfolioId))
                        .param("page", "1")
                        .param("perPage", "25")
                        .param("sort", "tradeDate,DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));

        mockMvc.perform(delete("/api/v1/instruments/{id}", instrumentId)
                        .with(httpBasic("user", "user1234")))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/v1/portfolios/{id}", portfolioId)
                        .with(httpBasic("user", "user1234")))
                .andExpect(status().isNoContent());
    }
}
