package com.example.commerce.account.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * 계좌 API의 개설, 멱등 거래, 잔액 검증, 원장 조회 계약을 통합 검증한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AccountControllerTest {

    @Container
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:17-alpine"))
                    .withDatabaseName("commerce")
                    .withUsername("commerce")
                    .withPassword("commerce-test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MeterRegistry meterRegistry;

    /**
     * 테스트 PostgreSQL 연결 정보를 Spring 환경에 등록한다.
     *
     * @param registry 동적 속성 레지스트리
     */
    @DynamicPropertySource
    static void infrastructureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    /**
     * 계좌를 개설하고 동일한 입금 멱등 키가 잔액을 한 번만 변경하는지 검증한다.
     *
     * @throws Exception MockMvc 요청 실패
     */
    @Test
    void createsAccountAndProcessesDepositIdempotently() throws Exception {
        String accountId = createAccount(UUID.randomUUID());
        String request = """
                {"amount":100.00,"memo":"초기 입금"}
                """;

        MvcResult first = mockMvc.perform(post("/api/v1/accounts/{id}/deposits", accountId)
                        .header("Idempotency-Key", "deposit-test-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.balanceAfter").value(100.0))
                .andReturn();

        JsonNode firstTransaction = objectMapper.readTree(first.getResponse().getContentAsByteArray());

        mockMvc.perform(post("/api/v1/accounts/{id}/deposits", accountId)
                        .header("Idempotency-Key", "deposit-test-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(firstTransaction.get("id").asText()))
                .andExpect(jsonPath("$.balanceAfter").value(100.0));

        mockMvc.perform(get("/api/v1/accounts/{id}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100.0));

        mockMvc.perform(get("/api/v1/accounts/{id}/transactions", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        assertThat(meterRegistry.get("commerce.business.operation.duration")
                .tag("operation", "account.deposit")
                .tag("outcome", "success")
                .timer()
                .count()).isGreaterThanOrEqualTo(2);
    }

    /**
     * 잔액보다 큰 출금 요청이 422로 거절되고 잔액이 유지되는지 검증한다.
     *
     * @throws Exception MockMvc 요청 실패
     */
    @Test
    void rejectsWithdrawalWhenBalanceIsInsufficient() throws Exception {
        String accountId = createAccount(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/accounts/{id}/withdrawals", accountId)
                        .header("Idempotency-Key", "withdraw-test-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":1.00,"memo":"출금"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("TRANSACTION_REJECTED"));

        mockMvc.perform(get("/api/v1/accounts/{id}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(0.0));
    }

    private String createAccount(UUID ownerId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ownerId":"%s","currency":"krw"}
                                """.formatted(ownerId)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.currency").value("KRW"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray()).get("id").asText();
    }
}
