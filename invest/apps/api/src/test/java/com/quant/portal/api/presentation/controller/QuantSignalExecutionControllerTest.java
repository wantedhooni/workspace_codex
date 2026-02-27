package com.quant.portal.api.presentation.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class QuantSignalExecutionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldExecuteBuyAndSellSignalsThroughTransactionFlow() throws Exception {
        long portfolioId = createPortfolio("Algo Portfolio");
        createDeposit(portfolioId, "100000");

        long instrumentId = createInstrument("NVDA_EXEC", "NVIDIA Execution");
        long strategyId = createStrategy("Execution Strategy");

        long buySignalId = createSignal(strategyId, instrumentId, "BUY", "2026-02-13");

        mockMvc.perform(post("/api/v1/quant-signals/{id}/execute", buySignalId)
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": %d,
                                  "quantity": 5,
                                  "unitPrice": 1000,
                                  "fee": 10,
                                  "tax": 0,
                                  "currencyCode": "KRW"
                                }
                                """.formatted(portfolioId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transactionType").value("BUY"))
                .andExpect(jsonPath("$.data.portfolioId").value(portfolioId))
                .andExpect(jsonPath("$.data.instrumentId").value(instrumentId))
                .andExpect(jsonPath("$.data.tradeDate").value("2026-02-13"));

        long sellSignalId = createSignal(strategyId, instrumentId, "SELL", "2026-02-14");

        mockMvc.perform(post("/api/v1/quant-signals/{id}/execute", sellSignalId)
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": %d,
                                  "quantity": 2,
                                  "unitPrice": 1200,
                                  "fee": 5,
                                  "tax": 0,
                                  "currencyCode": "KRW",
                                  "memo": "reduce position"
                                }
                                """.formatted(portfolioId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transactionType").value("SELL"))
                .andExpect(jsonPath("$.data.realizedPnl").isNotEmpty())
                .andExpect(jsonPath("$.data.memo").value(containsString("signal#" + sellSignalId)));
    }

    @Test
    void shouldRejectNonTradeSignalExecution() throws Exception {
        long portfolioId = createPortfolio("Algo Hold Portfolio");
        createDeposit(portfolioId, "50000");

        long instrumentId = createInstrument("MSFT_EXEC", "Microsoft Execution");
        long strategyId = createStrategy("Hold Strategy");
        long holdSignalId = createSignal(strategyId, instrumentId, "HOLD", "2026-02-15");

        mockMvc.perform(post("/api/v1/quant-signals/{id}/execute", holdSignalId)
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": %d,
                                  "quantity": 1,
                                  "unitPrice": 500,
                                  "currencyCode": "KRW"
                                }
                                """.formatted(portfolioId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.error.details.signalType").value("Only BUY and SELL signals can be executed"));
    }

    @Test
    void shouldRejectDuplicateSignalExecution() throws Exception {
        long portfolioId = createPortfolio("Algo Duplicate Portfolio");
        createDeposit(portfolioId, "80000");

        long instrumentId = createInstrument("AMZN_EXEC", "Amazon Execution");
        long strategyId = createStrategy("Duplicate Strategy");
        long buySignalId = createSignal(strategyId, instrumentId, "BUY", "2026-02-16");

        String executePayload = """
                {
                  "portfolioId": %d,
                  "quantity": 3,
                  "unitPrice": 1000,
                  "currencyCode": "KRW"
                }
                """.formatted(portfolioId);

        mockMvc.perform(post("/api/v1/quant-signals/{id}/execute", buySignalId)
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(executePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transactionType").value("BUY"));

        mockMvc.perform(post("/api/v1/quant-signals/{id}/execute", buySignalId)
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(executePayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("CONFLICT"))
                .andExpect(jsonPath("$.error.details.signalId").value("Signal can only be executed once"));
    }

    @Test
    void shouldReturnSignalExecutionHistory() throws Exception {
        long portfolioId = createPortfolio("Algo History Portfolio");
        createDeposit(portfolioId, "90000");

        long instrumentId = createInstrument("META_EXEC", "Meta Execution");
        long strategyId = createStrategy("History Strategy");
        long buySignalId = createSignal(strategyId, instrumentId, "BUY", "2026-02-17");

        mockMvc.perform(get("/api/v1/quant-signals/{id}/execution", buySignalId)
                        .with(httpBasic("user", "user1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(nullValue()));

        mockMvc.perform(post("/api/v1/quant-signals/{id}/execute", buySignalId)
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": %d,
                                  "quantity": 4,
                                  "unitPrice": 1000,
                                  "currencyCode": "KRW"
                                }
                                """.formatted(portfolioId)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/quant-signals/{id}/execution", buySignalId)
                        .with(httpBasic("user", "user1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.signalId").value(buySignalId))
                .andExpect(jsonPath("$.data.transactionId").isNotEmpty())
                .andExpect(jsonPath("$.data.executedBy").value("user"))
                .andExpect(jsonPath("$.data.executedAt").isNotEmpty());
    }

    private long createPortfolio(String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/portfolios")
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "baseCurrency": "KRW"
                                }
                                """.formatted(name)))
                .andExpect(status().isOk())
                .andReturn();
        return readId(result);
    }

    private void createDeposit(long portfolioId, String amount) throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": %d,
                                  "transactionType": "DEPOSIT",
                                  "tradeDate": "2026-02-11",
                                  "amount": %s,
                                  "currencyCode": "KRW"
                                }
                                """.formatted(portfolioId, amount)))
                .andExpect(status().isOk());
    }

    private long createInstrument(String ticker, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/instruments")
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ticker": "%s",
                                  "name": "%s",
                                  "marketCode": "US",
                                  "currencyCode": "USD"
                                }
                                """.formatted(ticker, name)))
                .andExpect(status().isOk())
                .andReturn();
        return readId(result);
    }

    private long createStrategy(String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/quant-strategies")
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "style": "MOMENTUM",
                                  "status": "ACTIVE",
                                  "rebalanceCycleDays": 20,
                                  "description": "execution test strategy"
                                }
                                """.formatted(name)))
                .andExpect(status().isOk())
                .andReturn();
        return readId(result);
    }

    private long createSignal(long strategyId, long instrumentId, String signalType, String signalDate) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/quant-signals")
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "strategyId": %d,
                                  "instrumentId": %d,
                                  "signalType": "%s",
                                  "signalDate": "%s",
                                  "score": 0.90,
                                  "confidence": 0.75,
                                  "rationale": "execution test signal"
                                }
                                """.formatted(strategyId, instrumentId, signalType, signalDate)))
                .andExpect(status().isOk())
                .andReturn();
        return readId(result);
    }

    private long readId(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }
}
