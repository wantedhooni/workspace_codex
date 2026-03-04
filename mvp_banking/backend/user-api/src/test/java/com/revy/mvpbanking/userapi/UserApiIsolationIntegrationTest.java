package com.revy.mvpbanking.userapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revy.mvpbanking.announcement.domain.Announcement;
import com.revy.mvpbanking.announcement.domain.AnnouncementAudience;
import com.revy.mvpbanking.announcement.domain.AnnouncementRepository;
import com.revy.mvpbanking.announcement.domain.AnnouncementSeverity;
import com.revy.mvpbanking.auth.domain.RefreshTokenRecord;
import com.revy.mvpbanking.auth.infrastructure.RefreshTokenStore;
import com.revy.mvpbanking.notification.domain.Notification;
import com.revy.mvpbanking.notification.domain.NotificationCategory;
import com.revy.mvpbanking.notification.domain.NotificationRecipientType;
import com.revy.mvpbanking.notification.domain.NotificationRepository;
import com.revy.mvpbanking.notification.domain.NotificationSeverity;
import com.revy.mvpbanking.user.domain.EndUserRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = UserApiApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("user-api")
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
class UserApiIsolationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private EndUserRepository endUserRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @MockitoBean
    private RefreshTokenStore refreshTokenStore;

    @Test
    void userApiAllowsUserLoginAndBlocksAdminEndpoints() throws Exception {
        doNothing().when(refreshTokenStore).save(anyString(), any(RefreshTokenRecord.class), anyLong());

        mockMvc.perform(post("/api/user/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@mvpbanking.local",
                                  "password": "User1234!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.principalType").value("USER"));

        mockMvc.perform(get("/api/admin/accounts"))
                .andExpect(status().isForbidden());
    }

    @Test
    void userApiExposesStockPositionsForCurrentUser() throws Exception {
        doNothing().when(refreshTokenStore).save(anyString(), any(RefreshTokenRecord.class), anyLong());

        String token = loginAsUser();

        String content = mockMvc.perform(get("/api/user/stock-positions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode positions = objectMapper.readTree(content).path("data");
        boolean hasValuedPosition = false;
        for (JsonNode position : positions) {
            if ("MVP_MARKET_DATA".equals(position.path("quoteSource").asText())
                    && position.hasNonNull("currentPrice")
                    && position.hasNonNull("marketValue")
                    && position.hasNonNull("unrealizedProfitLoss")) {
                hasValuedPosition = true;
                break;
            }
        }

        org.assertj.core.api.Assertions.assertThat(hasValuedPosition).isTrue();
    }

    @Test
    void userApiExposesExecutionHistoryForExecutedOrders() throws Exception {
        doNothing().when(refreshTokenStore).save(anyString(), any(RefreshTokenRecord.class), anyLong());

        String token = loginAsUser();
        String content = mockMvc.perform(get("/api/user/stock-orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode orders = objectMapper.readTree(content).path("data");
        boolean hasExecutionHistory = false;
        for (JsonNode order : orders) {
            if (order.path("executions").isArray() && order.path("executions").size() > 0) {
                hasExecutionHistory = true;
                break;
            }
        }

        org.assertj.core.api.Assertions.assertThat(hasExecutionHistory).isTrue();
    }

    @Test
    void userApiExposesPartiallyFilledOrdersForCurrentUser() throws Exception {
        doNothing().when(refreshTokenStore).save(anyString(), any(RefreshTokenRecord.class), anyLong());

        String token = loginAsUser();
        String content = mockMvc.perform(get("/api/user/stock-orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode orders = objectMapper.readTree(content).path("data");
        boolean hasPartialOrder = false;
        for (JsonNode order : orders) {
            if ("PARTIALLY_FILLED".equals(order.path("status").asText())
                    && order.path("remainingQuantity").decimalValue().compareTo(java.math.BigDecimal.ZERO) > 0) {
                hasPartialOrder = true;
                break;
            }
        }

        org.assertj.core.api.Assertions.assertThat(hasPartialOrder).isTrue();
    }

    @Test
    void userApiCreatesExchangeRequestBetweenOwnBankingAccounts() throws Exception {
        doNothing().when(refreshTokenStore).save(anyString(), any(RefreshTokenRecord.class), anyLong());

        String token = loginAsUser();
        String accountsContent = mockMvc.perform(get("/api/user/accounts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode accounts = objectMapper.readTree(accountsContent).path("data");
        JsonNode sourceAccount = findBankingAccountByCurrency(accounts, "USD");
        JsonNode destinationAccount = findBankingAccountByCurrency(accounts, "KRW");

        mockMvc.perform(post("/api/user/exchange-requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceAccountId": "%s",
                                  "destinationAccountId": "%s",
                                  "fromAmount": 250.0000
                                }
                                """.formatted(
                                        sourceAccount.path("id").asText(),
                                        destinationAccount.path("id").asText()
                                )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sourceAccountId").value(sourceAccount.path("id").asText()))
                .andExpect(jsonPath("$.data.destinationAccountId").value(destinationAccount.path("id").asText()))
                .andExpect(jsonPath("$.data.fromCurrency").value("USD"))
                .andExpect(jsonPath("$.data.toCurrency").value("KRW"))
                .andExpect(jsonPath("$.data.exchangeFeeAmount").isNumber())
                .andExpect(jsonPath("$.data.netToAmount").isNumber())
                .andExpect(jsonPath("$.data.status").value("PENDING_APPROVAL"));
    }

    @Test
    void userApiCreatesFundingRequestForOwnAccount() throws Exception {
        doNothing().when(refreshTokenStore).save(anyString(), any(RefreshTokenRecord.class), anyLong());

        String token = loginAsUser();
        String accountsContent = mockMvc.perform(get("/api/user/accounts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode activeAccount = findActiveAccount(objectMapper.readTree(accountsContent).path("data"));

        mockMvc.perform(post("/api/user/funding-requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": "%s",
                                  "requestType": "WITHDRAWAL",
                                  "amount": 25.0000,
                                  "note": "생활비 출금"
                                }
                                """.formatted(activeAccount.path("id").asText())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountId").value(activeAccount.path("id").asText()))
                .andExpect(jsonPath("$.data.requestType").value("WITHDRAWAL"))
                .andExpect(jsonPath("$.data.status").value("PENDING_APPROVAL"))
                .andExpect(jsonPath("$.data.accountNumber").isNotEmpty());
    }

    @Test
    void userApiExposesDashboardInsights() throws Exception {
        doNothing().when(refreshTokenStore).save(anyString(), any(RefreshTokenRecord.class), anyLong());

        String token = loginAsUser();
        String content = mockMvc.perform(get("/api/user/dashboard/insights")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode dashboard = objectMapper.readTree(content).path("data");
        org.assertj.core.api.Assertions.assertThat(dashboard.path("totalAssetsKrw").decimalValue()).isGreaterThan(java.math.BigDecimal.ZERO);
        org.assertj.core.api.Assertions.assertThat(dashboard.path("cashAssetsKrw").decimalValue()).isGreaterThan(java.math.BigDecimal.ZERO);
        org.assertj.core.api.Assertions.assertThat(dashboard.path("currencyExposures").isArray()).isTrue();
        org.assertj.core.api.Assertions.assertThat(dashboard.path("currencyExposures").size()).isGreaterThan(0);
        org.assertj.core.api.Assertions.assertThat(dashboard.path("attentionItems").isArray()).isTrue();
        org.assertj.core.api.Assertions.assertThat(dashboard.path("topPositions").isArray()).isTrue();
    }

    @Test
    void userApiListsAndMarksNotificationsRead() throws Exception {
        doNothing().when(refreshTokenStore).save(anyString(), any(RefreshTokenRecord.class), anyLong());

        var endUser = endUserRepository.findByEmail("user@mvpbanking.local").orElseThrow();
        Notification notification = notificationRepository.save(
                new Notification(
                        "USER-IT-NOTIFICATION-" + UUID.randomUUID(),
                        NotificationRecipientType.USER,
                        endUser.getId(),
                        NotificationCategory.PORTFOLIO,
                        NotificationSeverity.INFO,
                        "사용자 알림 테스트",
                        "알림 센터 읽음 처리를 검증합니다.",
                        "/notifications",
                        "TEST",
                        null
                )
        );

        String token = loginAsUser();
        String content = mockMvc.perform(get("/api/user/notifications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").isNumber())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode items = objectMapper.readTree(content).path("data").path("items");
        org.assertj.core.api.Assertions.assertThat(items.isArray()).isTrue();
        org.assertj.core.api.Assertions.assertThat(items.findValuesAsText("id")).contains(notification.getId().toString());

        mockMvc.perform(post("/api/user/notifications/" + notification.getId() + "/read")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(notification.getId().toString()))
                .andExpect(jsonPath("$.data.read").value(true))
                .andExpect(jsonPath("$.data.readAt").isNotEmpty());
    }

    @Test
    void userApiExposesPublishedAnnouncementsOnly() throws Exception {
        doNothing().when(refreshTokenStore).save(anyString(), any(RefreshTokenRecord.class), anyLong());

        Announcement announcement = announcementRepository.save(
                new Announcement(
                        "USER-IT-ANNOUNCEMENT-" + UUID.randomUUID(),
                        "사용자 공지 테스트",
                        "사용자 채널에 노출될 공지입니다.",
                        "활성 공지 목록 검증을 위한 테스트용 본문입니다.",
                        AnnouncementSeverity.WARNING,
                        AnnouncementAudience.USER,
                        true,
                        Instant.now().minusSeconds(300),
                        Instant.now().plusSeconds(86400),
                        "admin@mvpbanking.local"
                )
        );
        announcement.publish(Instant.now().minusSeconds(120));
        announcementRepository.save(announcement);

        String token = loginAsUser();
        String content = mockMvc.perform(get("/api/user/announcements")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode announcements = objectMapper.readTree(content).path("data");
        org.assertj.core.api.Assertions.assertThat(announcements.isArray()).isTrue();
        org.assertj.core.api.Assertions.assertThat(announcements.findValuesAsText("id")).contains(announcement.getId().toString());
    }

    private String loginAsUser() throws Exception {
        String content = mockMvc.perform(post("/api/user/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@mvpbanking.local",
                                  "password": "User1234!"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(content);
        return root.path("data").path("accessToken").asText();
    }

    private static JsonNode findBankingAccountByCurrency(JsonNode accounts, String currency) {
        for (JsonNode account : accounts) {
            if ("BANKING".equals(account.path("accountType").asText())
                    && currency.equalsIgnoreCase(account.path("currency").asText())) {
                return account;
            }
        }
        throw new IllegalStateException("Required banking account not found for currency: " + currency);
    }

    private static JsonNode findActiveAccount(JsonNode accounts) {
        for (JsonNode account : accounts) {
            if ("ACTIVE".equals(account.path("status").asText())) {
                return account;
            }
        }
        throw new IllegalStateException("Required active account not found");
    }
}
