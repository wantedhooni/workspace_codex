package com.quant.portal.api.presentation.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
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
class TransactionValidationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRejectBuyCreateWhenAmountProvided() throws Exception {
        Long portfolioId = createPortfolio("Validation Portfolio BUY");
        Long instrumentId = createInstrument("AAPLV1", "Validation Apple");

        mockMvc.perform(post("/api/v1/transactions")
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": %d,
                                  "instrumentId": %d,
                                  "transactionType": "BUY",
                                  "tradeDate": "2026-02-11",
                                  "quantity": 1,
                                  "unitPrice": 1000,
                                  "amount": 1000,
                                  "currencyCode": "KRW"
                                }
                                """.formatted(portfolioId, instrumentId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.error.message", containsString("amount must be null for transaction type BUY")))
                .andExpect(jsonPath("$.error.details.amount", containsString("amount must be null for transaction type BUY")));
    }

    @Test
    void shouldRejectDepositCreateWhenInstrumentProvided() throws Exception {
        Long portfolioId = createPortfolio("Validation Portfolio DEPOSIT");
        Long instrumentId = createInstrument("AAPLV2", "Validation Apple 2");

        mockMvc.perform(post("/api/v1/transactions")
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": %d,
                                  "instrumentId": %d,
                                  "transactionType": "DEPOSIT",
                                  "tradeDate": "2026-02-11",
                                  "amount": 1000,
                                  "currencyCode": "KRW"
                                }
                                """.formatted(portfolioId, instrumentId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.error.message", containsString("instrumentId must be null for transaction type DEPOSIT")))
                .andExpect(jsonPath("$.error.details.instrumentId", containsString("instrumentId must be null for transaction type DEPOSIT")));
    }

    @Test
    void shouldRejectDepositUpdateWhenInstrumentProvided() throws Exception {
        Long portfolioId = createPortfolio("Validation Portfolio UPDATE");
        Long depositTransactionId = createDeposit(portfolioId, 1000);

        mockMvc.perform(put("/api/v1/transactions/{id}", depositTransactionId)
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "instrumentId": 999,
                                  "tradeDate": "2026-02-11",
                                  "amount": 1000,
                                  "currencyCode": "KRW"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.error.message", containsString("instrumentId must be null for transaction type DEPOSIT")))
                .andExpect(jsonPath("$.error.details.instrumentId", containsString("instrumentId must be null for transaction type DEPOSIT")));
    }

    @Test
    void shouldRejectBuyCreateWhenQuantityIsZero() throws Exception {
        Long portfolioId = createPortfolio("Validation Portfolio Quantity");
        Long instrumentId = createInstrument("AAPLV3", "Validation Apple 3");

        mockMvc.perform(post("/api/v1/transactions")
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": %d,
                                  "instrumentId": %d,
                                  "transactionType": "BUY",
                                  "tradeDate": "2026-02-11",
                                  "quantity": 0,
                                  "unitPrice": 1000,
                                  "currencyCode": "KRW"
                                }
                                """.formatted(portfolioId, instrumentId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.error.details.quantity", containsString("greater than 0")));
    }

    @Test
    void shouldRejectCreateWhenFeeIsNegative() throws Exception {
        Long portfolioId = createPortfolio("Validation Portfolio Fee");

        mockMvc.perform(post("/api/v1/transactions")
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": %d,
                                  "transactionType": "DEPOSIT",
                                  "tradeDate": "2026-02-11",
                                  "amount": 1000,
                                  "fee": -1,
                                  "currencyCode": "KRW"
                                }
                                """.formatted(portfolioId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.error.details.fee", containsString("greater than or equal to 0")));
    }

    @Test
    void shouldRejectCreateWhenPortfolioIdIsZero() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 0,
                                  "transactionType": "DEPOSIT",
                                  "tradeDate": "2026-02-11",
                                  "amount": 1000,
                                  "currencyCode": "KRW"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.error.details.portfolioId", containsString("greater than 0")));
    }

    @Test
    void shouldRejectUpdateWhenInstrumentIdIsNegative() throws Exception {
        Long portfolioId = createPortfolio("Validation Portfolio InstrumentId Negative");
        Long depositTransactionId = createDeposit(portfolioId, 1000);

        mockMvc.perform(put("/api/v1/transactions/{id}", depositTransactionId)
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "instrumentId": -1,
                                  "tradeDate": "2026-02-11",
                                  "amount": 1000,
                                  "currencyCode": "KRW"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.error.details.instrumentId", containsString("greater than 0")));
    }

    private Long createPortfolio(String name) throws Exception {
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

        return extractId(result);
    }

    private Long createInstrument(String ticker, String name) throws Exception {
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

        return extractId(result);
    }

    private Long createDeposit(Long portfolioId, int amount) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/transactions")
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": %d,
                                  "transactionType": "DEPOSIT",
                                  "tradeDate": "2026-02-11",
                                  "amount": %d,
                                  "currencyCode": "KRW"
                                }
                                """.formatted(portfolioId, amount)))
                .andExpect(status().isOk())
                .andReturn();

        return extractId(result);
    }

    private Long extractId(MvcResult result) throws Exception {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.path("data").path("id").asLong();
    }
}
