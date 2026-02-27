package com.quant.portal.api.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quant.portal.api.infrastructure.jpa.repository.InstrumentRepository;
import com.quant.portal.api.infrastructure.jpa.repository.PortfolioRepository;
import com.quant.portal.api.infrastructure.jpa.repository.PortfolioTransactionRepository;
import com.quant.portal.domain.portfolio.entity.Instrument;
import com.quant.portal.domain.portfolio.entity.Portfolio;
import com.quant.portal.domain.portfolio.entity.PortfolioTransaction;
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
class AuditApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private PortfolioTransactionRepository portfolioTransactionRepository;

    @Test
    void shouldPopulatePortfolioAuditFieldsThroughApiCredentials() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/portfolios")
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "API Audit Portfolio",
                                  "baseCurrency": "KRW"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        Long portfolioId = extractId(createResult);

        Portfolio createdPortfolio = portfolioRepository.findById(portfolioId).orElseThrow();
        assertThat(createdPortfolio.getCreatedBy()).isEqualTo("user");
        assertThat(createdPortfolio.getUpdatedBy()).isEqualTo("user");

        mockMvc.perform(put("/api/v1/portfolios/{id}", portfolioId)
                        .with(httpBasic("admin", "admin1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "API Audit Portfolio Renamed"
                                }
                                """))
                .andExpect(status().isOk());

        Portfolio updatedPortfolio = portfolioRepository.findById(portfolioId).orElseThrow();
        assertThat(updatedPortfolio.getCreatedBy()).isEqualTo("user");
        assertThat(updatedPortfolio.getUpdatedBy()).isEqualTo("admin");
    }

    @Test
    void shouldPopulateInstrumentAuditFieldsThroughApiCredentials() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/instruments")
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ticker": "NFLX",
                                  "name": "Netflix",
                                  "marketCode": "US",
                                  "currencyCode": "USD"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        Long instrumentId = extractId(createResult);

        Instrument createdInstrument = instrumentRepository.findById(instrumentId).orElseThrow();
        assertThat(createdInstrument.getCreatedBy()).isEqualTo("user");
        assertThat(createdInstrument.getUpdatedBy()).isEqualTo("user");

        mockMvc.perform(put("/api/v1/instruments/{id}", instrumentId)
                        .with(httpBasic("admin", "admin1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Netflix Updated"
                                }
                                """))
                .andExpect(status().isOk());

        Instrument updatedInstrument = instrumentRepository.findById(instrumentId).orElseThrow();
        assertThat(updatedInstrument.getCreatedBy()).isEqualTo("user");
        assertThat(updatedInstrument.getUpdatedBy()).isEqualTo("admin");
    }

    @Test
    void shouldPopulateTransactionAuditFieldsThroughApiCredentials() throws Exception {
        MvcResult portfolioResult = mockMvc.perform(post("/api/v1/portfolios")
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "API Tx Audit Portfolio",
                                  "baseCurrency": "KRW"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();
        Long portfolioId = extractId(portfolioResult);

        MvcResult instrumentResult = mockMvc.perform(post("/api/v1/instruments")
                        .with(httpBasic("user", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ticker": "MSFT",
                                  "name": "Microsoft",
                                  "marketCode": "US",
                                  "currencyCode": "USD"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();
        Long instrumentId = extractId(instrumentResult);

        mockMvc.perform(post("/api/v1/transactions")
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
                .andExpect(status().isOk());

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
                .andReturn();

        Long buyTransactionId = extractId(buyResult);

        PortfolioTransaction createdTransaction = portfolioTransactionRepository.findById(buyTransactionId).orElseThrow();
        assertThat(createdTransaction.getCreatedBy()).isEqualTo("user");
        assertThat(createdTransaction.getUpdatedBy()).isEqualTo("user");

        mockMvc.perform(put("/api/v1/transactions/{id}", buyTransactionId)
                        .with(httpBasic("admin", "admin1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "instrumentId": %d,
                                  "tradeDate": "2026-02-11",
                                  "quantity": 2,
                                  "unitPrice": 950,
                                  "fee": 10,
                                  "tax": 0,
                                  "currencyCode": "KRW",
                                  "memo": "api updated"
                                }
                                """.formatted(instrumentId)))
                .andExpect(status().isOk());

        PortfolioTransaction updatedTransaction = portfolioTransactionRepository.findById(buyTransactionId).orElseThrow();
        assertThat(updatedTransaction.getCreatedBy()).isEqualTo("user");
        assertThat(updatedTransaction.getUpdatedBy()).isEqualTo("admin");
    }

    private Long extractId(MvcResult mvcResult) throws Exception {
        JsonNode node = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        return node.path("data").path("id").asLong();
    }
}
