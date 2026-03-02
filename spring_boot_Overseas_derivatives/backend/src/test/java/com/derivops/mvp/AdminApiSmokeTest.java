package com.derivops.mvp;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.derivops.mvp.account.Account;
import com.derivops.mvp.audit.infrastructure.AuditLogRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.hibernate.envers.AuditReaderFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AdminApiSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void loginAndReadAccountsShouldSucceed() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "opsadmin",
                                  "password": "admin123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();

        JsonNode body = objectMapper.readTree(login.getResponse().getContentAsString());
        String token = body.get("accessToken").asText();

        mockMvc.perform(get("/api/v1/accounts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void unauthorizedAccessShouldFail() throws Exception {
        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldPersistAuditLogViaR2dbcOnLogin() throws Exception {
        long before = auditLogRepository.count();

        login("opsadmin", "admin123!");

        long after = waitForAuditLogCountToExceed(before, Duration.ofSeconds(5));
        Assertions.assertThat(after).isGreaterThan(before);
    }

    @Test
    void shouldRefreshTokensAndRejectRefreshTokenAsAccessToken() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "opsadmin",
                                  "password": "admin123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        JsonNode loginBody = objectMapper.readTree(login.getResponse().getContentAsString());
        String refreshToken = loginBody.get("refreshToken").asText();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.role").value("OPS_ADMIN"));

        mockMvc.perform(get("/api/v1/accounts")
                        .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional(readOnly = true)
    void shouldEnableEnversForDomainEntitiesButExcludeAuditLog() {
        Assertions.assertThat(tableExists("REVINFO")).isTrue();
        Assertions.assertThat(tableExists("ACCOUNTS_AUD")).isTrue();
        Assertions.assertThat(tableExists("AUDIT_LOGS_AUD")).isFalse();

        List<Number> revisions = AuditReaderFactory.get(entityManager).getRevisions(Account.class, 1L);
        Assertions.assertThat(revisions).isNotEmpty();
    }

    private long waitForAuditLogCountToExceed(long baseline, Duration timeout) throws InterruptedException {
        long deadline = System.nanoTime() + timeout.toNanos();
        long current = auditLogRepository.count();
        while (current <= baseline && System.nanoTime() < deadline) {
            Thread.sleep(100);
            current = auditLogRepository.count();
        }
        return current;
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where upper(table_name) = ?",
                Integer.class,
                tableName
        );
        return count != null && count > 0;
    }

    @Test
    void shouldBlockRequestOnNonActiveAccount() throws Exception {
        String token = login("opsadmin", "admin123!");

        mockMvc.perform(post("/api/v1/cash-requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": 2,
                                  "type": "DEPOSIT",
                                  "amount": 1000,
                                  "currency": "EUR",
                                  "reason": "Margin top-up for next session",
                                  "priority": "NORMAL",
                                  "valueDate": "2099-01-01"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("not operable")));
    }

    @Test
    void shouldEnforceFourEyesControlForApproval() throws Exception {
        String requesterToken = login("opsadmin", "admin123!");

        MvcResult created = mockMvc.perform(post("/api/v1/cash-requests")
                        .header("Authorization", "Bearer " + requesterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": 1,
                                  "type": "DEPOSIT",
                                  "amount": 1500,
                                  "currency": "USD",
                                  "reason": "Intraday liquidity adjustment",
                                  "priority": "NORMAL",
                                  "valueDate": "2099-01-01"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn();

        UUID requestId = UUID.fromString(objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText());

        mockMvc.perform(post("/api/v1/requests/{requestId}/approve", requestId)
                        .header("Authorization", "Bearer " + requesterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "Self-check done"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("4-eyes")));

        String approverToken = login("opsadmin2", "admin234!");
        mockMvc.perform(post("/api/v1/requests/{requestId}/approve", requestId)
                        .header("Authorization", "Bearer " + approverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "Second checker validated balances and controls"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewedBy").value("opsadmin2"));
    }

    @Test
    void shouldReadDbMenusAndQuartzSchedules() throws Exception {
        String token = login("opsadmin", "admin123!");

        mockMvc.perform(get("/api/v1/menus/my")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].menuKey").value("dashboard"))
                .andExpect(jsonPath("$[1].menuKey").value("reference-root"))
                .andExpect(jsonPath("$[1].children[0].menuKey").value("domain-terms"))
                .andExpect(jsonPath("$[2].menuKey").value("operations-root"))
                .andExpect(jsonPath("$[2].children[0].menuKey").value("accounts"))
                .andExpect(jsonPath("$[2].children[0].depth").value(1))
                .andExpect(jsonPath("$[2].children[0].children").isEmpty());

        mockMvc.perform(get("/api/v1/domain-terms")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].domainName").isNotEmpty())
                .andExpect(jsonPath("$[0].koreanName").isNotEmpty());

        mockMvc.perform(get("/api/v1/exchange-rates/quote")
                        .header("Authorization", "Bearer " + token)
                        .param("fromCurrency", "USD")
                        .param("toCurrency", "KRW")
                        .param("amount", "10000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exchangeRate").isNotEmpty())
                .andExpect(jsonPath("$.convertedAmount").isNotEmpty());

        mockMvc.perform(get("/api/v1/batches/schedules")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].triggerName").isNotEmpty());
    }

    @Test
    void shouldApplyRiskSoftLimitAndForceManualReview() throws Exception {
        String token = login("opsadmin", "admin123!");

        mockMvc.perform(post("/api/v1/fx-requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": 1,
                                  "fromCurrency": "USD",
                                  "toCurrency": "KRW",
                                  "amount": 1250000,
                                  "reason": "USD funding tranche one",
                                  "priority": "NORMAL",
                                  "valueDate": "2099-01-02"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/fx-requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": 1,
                                  "fromCurrency": "USD",
                                  "toCurrency": "JPY",
                                  "amount": 1249000,
                                  "reason": "USD funding tranche two",
                                  "priority": "NORMAL",
                                  "valueDate": "2099-01-02"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/fx-requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": 1,
                                  "fromCurrency": "USD",
                                  "toCurrency": "EUR",
                                  "amount": 10000,
                                  "reason": "USD liquidity top-up after soft limit",
                                  "priority": "NORMAL",
                                  "valueDate": "2099-01-02"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manualReviewRequired").value(true))
                .andExpect(jsonPath("$.controlLimitPolicyId").isNotEmpty())
                .andExpect(jsonPath("$.controlLimitPolicySource").value(org.hamcrest.Matchers.containsString("CME/USD")))
                .andExpect(jsonPath("$.projectedDailyExposure").isNotEmpty())
                .andExpect(jsonPath("$.exchangeRate").isNotEmpty())
                .andExpect(jsonPath("$.expectedToAmount").isNotEmpty());
    }

    @Test
    void shouldBlockRequestWhenPerRequestRiskLimitExceeded() throws Exception {
        String token = login("opsadmin", "admin123!");

        mockMvc.perform(post("/api/v1/fx-requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": 1,
                                  "fromCurrency": "USD",
                                  "toCurrency": "EUR",
                                  "amount": 1300001,
                                  "reason": "Attempt over per-request risk limit",
                                  "priority": "NORMAL",
                                  "valueDate": "2099-01-02"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("per-request risk limit")));
    }

    @Test
    void shouldCreateAndListOpsCases() throws Exception {
        String token = login("opsadmin", "admin123!");

        mockMvc.perform(post("/api/v1/ops-cases")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "category": "REQUEST_FAILURE",
                                  "severity": "HIGH",
                                  "title": "Failed broker callback for cash withdraw",
                                  "description": "Broker callback timeout observed on withdraw instruction.",
                                  "assignee": "opsadmin",
                                  "dueAt": "2099-01-02T10:00:00Z",
                                  "linkedType": "CASH_REQUEST",
                                  "linkedId": "DEMO-REQUEST-01",
                                  "accountId": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseNo").isNotEmpty())
                .andExpect(jsonPath("$.status").value("OPEN"));

        mockMvc.perform(get("/api/v1/ops-cases")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].caseNo").isNotEmpty());
    }

    @Test
    void shouldCreateStockPurchaseAndReflectPositionLedgerAndJournal() throws Exception {
        String token = login("opsadmin", "admin123!");

        MvcResult created = mockMvc.perform(post("/api/v1/stock-purchases")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": 1,
                                  "symbol": "MSFT",
                                  "market": "NASDAQ",
                                  "currency": "USD",
                                  "tradeDate": "2099-01-03",
                                  "settlementDate": "2099-01-05",
                                  "quantity": 10,
                                  "price": 420.15,
                                  "feeAmount": 12.35
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.grossAmount").value(4201.5000))
                .andExpect(jsonPath("$.netAmount").value(4213.8500))
                .andReturn();

        String purchaseId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/v1/stock-positions")
                        .header("Authorization", "Bearer " + token)
                        .param("accountId", "1")
                        .param("symbol", "MSFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].symbol").value("MSFT"))
                .andExpect(jsonPath("$.content[0].quantity").value(10.0000));

        mockMvc.perform(get("/api/v1/ledger-entries")
                        .header("Authorization", "Bearer " + token)
                        .param("referenceId", purchaseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].referenceType").value("STOCK_PURCHASE"))
                .andExpect(jsonPath("$.content[0].amountChange").value(4213.8500));

        mockMvc.perform(get("/api/v1/journal-entries")
                        .header("Authorization", "Bearer " + token)
                        .param("referenceId", purchaseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].journalNo").isNotEmpty());
    }

    @Test
    void shouldReadPortfolioOverview() throws Exception {
        String token = login("opsadmin", "admin123!");

        mockMvc.perform(get("/api/v1/portfolios/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(1))
                .andExpect(jsonPath("$.cashBalances").isArray())
                .andExpect(jsonPath("$.holdings").isArray())
                .andExpect(jsonPath("$.recentPurchases").isArray())
                .andExpect(jsonPath("$.stockCostByCurrency").isArray());
    }

    @Test
    void shouldGenerateStockRecommendationDraft() throws Exception {
        String token = login("opsadmin", "admin123!");

        mockMvc.perform(post("/api/v1/stock-recommendations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": 1,
                                  "riskProfile": "BALANCED",
                                  "investmentHorizon": "MEDIUM_TERM",
                                  "maxRecommendations": 3,
                                  "preferredMarkets": ["NASDAQ"],
                                  "candidateSymbols": ["MSFT", "AMD", "QQQ"],
                                  "operatorView": "기존 NVDA 비중이 높은 편이라 분산 후보 위주로 보고 싶음"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").isNotEmpty())
                .andExpect(jsonPath("$.summary").isNotEmpty())
                .andExpect(jsonPath("$.recommendations").isArray())
                .andExpect(jsonPath("$.recommendations[0].symbol").isNotEmpty())
                .andExpect(jsonPath("$.disclaimer").isNotEmpty());
    }

    private String login(String username, String password) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(login.getResponse().getContentAsString());
        return body.get("accessToken").asText();
    }
}
