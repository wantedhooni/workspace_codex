package com.revy.mvpbanking.adminapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revy.mvpbanking.account.domain.Account;
import com.revy.mvpbanking.account.domain.AccountRepository;
import com.revy.mvpbanking.announcement.domain.AnnouncementRepository;
import com.revy.mvpbanking.approval.domain.ApprovalRequest;
import com.revy.mvpbanking.approval.domain.ApprovalRequestRepository;
import com.revy.mvpbanking.approval.domain.ApprovalStatus;
import com.revy.mvpbanking.approval.domain.ApprovalTargetType;
import com.revy.mvpbanking.auth.domain.RefreshTokenRecord;
import com.revy.mvpbanking.auth.infrastructure.RefreshTokenStore;
import com.revy.mvpbanking.exchange.domain.ExchangeRequestStatus;
import com.revy.mvpbanking.exchange.domain.ExchangeRequest;
import com.revy.mvpbanking.exchange.domain.ExchangeRequestRepository;
import com.revy.mvpbanking.admin.domain.AdminUserRepository;
import com.revy.mvpbanking.customer.domain.CustomerRepository;
import com.revy.mvpbanking.linkedaccount.domain.LinkedBankAccount;
import com.revy.mvpbanking.linkedaccount.domain.LinkedBankAccountRepository;
import com.revy.mvpbanking.notification.domain.Notification;
import com.revy.mvpbanking.notification.domain.NotificationCategory;
import com.revy.mvpbanking.notification.domain.NotificationRecipientType;
import com.revy.mvpbanking.notification.domain.NotificationRepository;
import com.revy.mvpbanking.notification.domain.NotificationSeverity;
import com.revy.mvpbanking.stock.domain.StockOrder;
import com.revy.mvpbanking.stock.domain.StockOrderExecutionRepository;
import com.revy.mvpbanking.stock.domain.StockOrderRepository;
import com.revy.mvpbanking.stock.domain.StockOrderStatus;
import com.revy.mvpbanking.stock.domain.StockOrderSide;
import com.revy.mvpbanking.stock.domain.StockPositionRepository;
import com.revy.mvpbanking.transaction.domain.TransactionEntryRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AdminApiApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("admin-api")
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
class AdminApiIsolationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApprovalRequestRepository approvalRequestRepository;

    @Autowired
    private ExchangeRequestRepository exchangeRequestRepository;

    @Autowired
    private StockOrderRepository stockOrderRepository;

    @Autowired
    private StockOrderExecutionRepository stockOrderExecutionRepository;

    @Autowired
    private StockPositionRepository stockPositionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LinkedBankAccountRepository linkedBankAccountRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TransactionEntryRepository transactionEntryRepository;

    @MockitoBean
    private RefreshTokenStore refreshTokenStore;

    @Test
    void adminApiAllowsAdminLoginAndBlocksUserEndpoints() throws Exception {
        doNothing().when(refreshTokenStore).save(anyString(), any(RefreshTokenRecord.class), anyLong());

        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@mvpbanking.local",
                                  "password": "Admin1234!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.principalType").value("ADMIN"));

        mockMvc.perform(get("/api/user/accounts"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminApiExposesValuedStockPositions() throws Exception {
        doNothing().when(refreshTokenStore).save(anyString(), any(RefreshTokenRecord.class), anyLong());

        String token = loginAsAdmin();

        String content = mockMvc.perform(get("/api/admin/stock-positions")
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

        assertThat(hasValuedPosition).isTrue();
    }

    @Test
    void adminApiExposesExecutionHistoryForApprovedOrders() throws Exception {
        doNothing().when(refreshTokenStore).save(anyString(), any(RefreshTokenRecord.class), anyLong());

        String token = loginAsAdmin();
        String content = mockMvc.perform(get("/api/admin/stock-orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode orders = objectMapper.readTree(content).path("data");
        boolean hasExecutedOrder = false;
        for (JsonNode order : orders) {
            if (order.path("executions").isArray() && order.path("executions").size() > 0) {
                hasExecutedOrder = true;
                break;
            }
        }

        assertThat(hasExecutedOrder).isTrue();
        assertThat(orders.isArray()).isTrue();
        assertThat(orders.size()).isGreaterThan(0);
        assertThat(orders.get(0).path("timeInForce").asText()).isNotBlank();
        assertThat(orders.get(0).path("expiresAt").asText()).isNotBlank();
    }

    @Test
    void adminApiExposesPartiallyFilledOrders() throws Exception {
        doNothing().when(refreshTokenStore).save(anyString(), any(RefreshTokenRecord.class), anyLong());

        String token = loginAsAdmin();
        String content = mockMvc.perform(get("/api/admin/stock-orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode orders = objectMapper.readTree(content).path("data");
        boolean hasPartialOrder = false;
        for (JsonNode order : orders) {
            if ("PARTIALLY_FILLED".equals(order.path("status").asText())
                    && order.path("remainingQuantity").decimalValue().compareTo(BigDecimal.ZERO) > 0) {
                hasPartialOrder = true;
                break;
            }
        }

        assertThat(hasPartialOrder).isTrue();
    }

    @Test
    void adminApiExposesOperationsOverviewSnapshot() throws Exception {
        doNothing().when(refreshTokenStore).save(anyString(), any(RefreshTokenRecord.class), anyLong());

        String token = loginAsAdmin();
        String content = mockMvc.perform(get("/api/admin/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode overview = objectMapper.readTree(content).path("data");
        assertThat(overview.path("metrics").path("pendingApprovals").asLong()).isGreaterThan(0);
        assertThat(overview.path("metrics").path("reviewRequiredCustomers").asLong()).isGreaterThan(0);
        assertThat(overview.path("metrics").path("pendingFundingRequests").asLong()).isGreaterThanOrEqualTo(0);
        assertThat(overview.path("metrics").path("pendingInstructionVolumeKrw").decimalValue()).isGreaterThan(BigDecimal.ZERO);
        assertThat(
                overview.path("marketStatus").path("freshFxPairs").asLong()
                        + overview.path("marketStatus").path("staleFxPairs").asLong()
        ).isGreaterThan(0);
        assertThat(overview.path("alerts").isArray()).isTrue();
        assertThat(overview.path("alerts").size()).isGreaterThan(0);
    }

    @Test
    void adminApiListsAndMarksNotificationsRead() throws Exception {
        doNothing().when(refreshTokenStore).save(anyString(), any(RefreshTokenRecord.class), anyLong());

        var adminUser = adminUserRepository.findByEmail("admin@mvpbanking.local").orElseThrow();
        Notification notification = notificationRepository.save(
                new Notification(
                        "ADMIN-IT-NOTIFICATION-" + UUID.randomUUID(),
                        NotificationRecipientType.ADMIN,
                        adminUser.getId(),
                        NotificationCategory.APPROVAL,
                        NotificationSeverity.ACTION_REQUIRED,
                        "관리자 알림 테스트",
                        "운영 인박스 읽음 처리를 검증합니다.",
                        "/notifications",
                        "TEST",
                        null
                )
        );

        String token = loginAsAdmin();
        String content = mockMvc.perform(get("/api/admin/notifications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").isNumber())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode items = objectMapper.readTree(content).path("data").path("items");
        assertThat(items.isArray()).isTrue();
        assertThat(items.findValuesAsText("id")).contains(notification.getId().toString());

        mockMvc.perform(post("/api/admin/notifications/" + notification.getId() + "/read")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(notification.getId().toString()))
                .andExpect(jsonPath("$.data.read").value(true))
                .andExpect(jsonPath("$.data.readAt").isNotEmpty());
    }

    @Test
    void adminApiExposesFundingRequests() throws Exception {
        doNothing().when(refreshTokenStore).save(anyString(), any(RefreshTokenRecord.class), anyLong());

        String token = loginAsAdmin();
        String content = mockMvc.perform(get("/api/admin/funding-requests")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode requests = objectMapper.readTree(content).path("data");
        assertThat(requests.isArray()).isTrue();
        assertThat(requests.size()).isGreaterThan(0);
        assertThat(requests.get(0).path("requestType").asText()).isNotBlank();
        assertThat(requests.get(0).path("accountNumber").asText()).isNotBlank();
        assertThat(requests.get(0).has("serviceFeeAmount")).isTrue();
        assertThat(requests.get(0).has("totalDebitAmount")).isTrue();
        assertThat(requests.get(0).has("sameDaySettlementEligible")).isTrue();
        assertThat(requests.get(0).has("manualReviewRequired")).isTrue();
        assertThat(requests.get(0).has("dailyLimitExceeded")).isTrue();
    }

    @Test
    void adminApiExposesLinkedBankAccounts() throws Exception {
        doNothing().when(refreshTokenStore).save(anyString(), any(RefreshTokenRecord.class), anyLong());

        String token = loginAsAdmin();
        String content = mockMvc.perform(get("/api/admin/linked-bank-accounts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode linkedAccounts = objectMapper.readTree(content).path("data");
        assertThat(linkedAccounts.isArray()).isTrue();
        assertThat(linkedAccounts.size()).isGreaterThan(0);
        assertThat(linkedAccounts.get(0).path("bankName").asText()).isNotBlank();
        assertThat(linkedAccounts.get(0).path("maskedAccountNumber").asText()).isNotBlank();
    }

    @Test
    void adminApiBlocksLinkedBankAccount() throws Exception {
        doNothing().when(refreshTokenStore).save(anyString(), any(RefreshTokenRecord.class), anyLong());

        var customer = customerRepository.findByCustomerNumber("CUST-100001").orElseThrow();
        LinkedBankAccount linkedBankAccount = linkedBankAccountRepository.save(
                new LinkedBankAccount(
                        customer.getId(),
                        customer.getEmail(),
                        "Woori Bank",
                        "테스트 출금 계좌",
                        "MVP User",
                        "020-555-" + UUID.randomUUID().toString().substring(0, 6),
                        false
                )
        );

        String token = loginAsAdmin();
        mockMvc.perform(post("/api/admin/linked-bank-accounts/{linkedBankAccountId}/block", linkedBankAccount.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(linkedBankAccount.getId().toString()))
                .andExpect(jsonPath("$.data.status").value("BLOCKED"))
                .andExpect(jsonPath("$.data.blockReasonCode").value("OPS_BLOCKED"))
                .andExpect(jsonPath("$.data.blockedAt").isNotEmpty())
                .andExpect(jsonPath("$.data.primaryWithdrawal").value(false));
    }

    @Test
    void adminApiActivatesPendingLinkedBankAccount() throws Exception {
        doNothing().when(refreshTokenStore).save(anyString(), any(RefreshTokenRecord.class), anyLong());

        var customer = customerRepository.findByCustomerNumber("CUST-100001").orElseThrow();
        LinkedBankAccount linkedBankAccount = linkedBankAccountRepository.save(
                LinkedBankAccount.pendingVerification(
                        customer.getId(),
                        customer.getEmail(),
                        "Kakao Bank",
                        "급여 수령 계좌",
                        "MVP User",
                        "777-000-" + UUID.randomUUID().toString().substring(0, 6),
                        true,
                        "MVP-2468"
                )
        );

        String token = loginAsAdmin();
        mockMvc.perform(post("/api/admin/linked-bank-accounts/{linkedBankAccountId}/activate", linkedBankAccount.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(linkedBankAccount.getId().toString()))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.primaryWithdrawal").value(true))
                .andExpect(jsonPath("$.data.verifiedAt").isNotEmpty());
    }

    @Test
    void adminApiCreatesAndPublishesAnnouncement() throws Exception {
        doNothing().when(refreshTokenStore).save(anyString(), any(RefreshTokenRecord.class), anyLong());

        String token = loginAsAdmin();
        String createContent = mockMvc.perform(post("/api/admin/announcements")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "신규 서비스 공지",
                                  "summary": "배포 전 사전 공지입니다.",
                                  "body": "고객 채널에 노출할 공지 본문입니다.",
                                  "severity": "WARNING",
                                  "audience": "ALL",
                                  "pinned": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String announcementId = objectMapper.readTree(createContent).path("data").path("id").asText();

        mockMvc.perform(post("/api/admin/announcements/{announcementId}/publish", announcementId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(announcementId))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.data.publishedAt").isNotEmpty());

        assertThat(announcementRepository.findById(UUID.fromString(announcementId))).isPresent();
    }

    @Test
    void approvingFxAndStockRequestsUpdatesBalancesAndPositions() throws Exception {
        doNothing().when(refreshTokenStore).save(anyString(), any(RefreshTokenRecord.class), anyLong());

        String token = loginAsAdmin();

        ExchangeRequest templateExchange = exchangeRequestRepository.findByRequestNumber("FX-DEMO-0001").orElseThrow();
        Account sourceAccount = accountRepository.findById(templateExchange.getSourceAccountId()).orElseThrow();
        sourceAccount.credit(new BigDecimal("5000.0000"));
        sourceAccount = accountRepository.save(sourceAccount);
        Account destinationAccount = accountRepository.findById(templateExchange.getDestinationAccountId()).orElseThrow();
        BigDecimal sourceBalanceBefore = sourceAccount.getBalance();
        BigDecimal destinationBalanceBefore = destinationAccount.getBalance();
        var requestSuffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        ExchangeRequest exchangeRequest = exchangeRequestRepository.save(
                new ExchangeRequest(
                        templateExchange.getCustomerId(),
                        templateExchange.getSourceAccountId(),
                        templateExchange.getDestinationAccountId(),
                        "FX-IT-" + requestSuffix,
                        templateExchange.getFromCurrency(),
                        templateExchange.getToCurrency(),
                        new BigDecimal("1750.0000"),
                        new BigDecimal("1338.250000"),
                        new BigDecimal("1750.0000").multiply(new BigDecimal("1338.250000")).setScale(4, RoundingMode.HALF_UP),
                        ExchangeRequestStatus.PENDING_APPROVAL
                )
        );
        ApprovalRequest exchangeApproval = approvalRequestRepository.save(
                new ApprovalRequest(
                        ApprovalTargetType.FX_EXCHANGE,
                        exchangeRequest.getId(),
                        "FX exchange " + exchangeRequest.getRequestNumber(),
                        "FX integration approval",
                        ApprovalStatus.PENDING,
                        "approval-test@mvpbanking.local"
                )
        );

        mockMvc.perform(post("/api/admin/approvals/{approvalId}/approve", exchangeApproval.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "FX settlement approved"
                                }
                                """))
                .andExpect(status().isOk());

        ExchangeRequest approvedExchange = exchangeRequestRepository.findById(exchangeRequest.getId()).orElseThrow();
        Account debitedSourceAccount = accountRepository.findById(sourceAccount.getId()).orElseThrow();
        Account creditedDestinationAccount = accountRepository.findById(destinationAccount.getId()).orElseThrow();

        assertThat(approvedExchange.getStatus()).isEqualTo(ExchangeRequestStatus.APPROVED);
        assertThat(approvedExchange.getSettledAt()).isNotNull();
        assertThat(approvedExchange.getSourceTransactionNumber()).isNotBlank();
        assertThat(approvedExchange.getDestinationTransactionNumber()).isNotBlank();
        assertThat(approvedExchange.getExchangeFeeAmount()).isGreaterThan(BigDecimal.ZERO);
        assertThat(approvedExchange.getNetToAmount()).isEqualByComparingTo(approvedExchange.getToAmount().subtract(approvedExchange.getExchangeFeeAmount()));
        assertThat(debitedSourceAccount.getBalance()).isEqualByComparingTo(sourceBalanceBefore.subtract(exchangeRequest.getFromAmount()));
        assertThat(creditedDestinationAccount.getBalance()).isEqualByComparingTo(destinationBalanceBefore.add(exchangeRequest.getNetToAmount()));
        assertThat(transactionEntryRepository.findByTransactionNumber(approvedExchange.getSourceTransactionNumber())).isPresent();
        assertThat(transactionEntryRepository.findByTransactionNumber(approvedExchange.getDestinationTransactionNumber())).isPresent();

        StockOrder templateOrder = stockOrderRepository.findByOrderNumber("ORD-DEMO-0001").orElseThrow();
        Account securitiesAccount = accountRepository.findById(templateOrder.getAccountId()).orElseThrow();
        securitiesAccount.credit(new BigDecimal("10000.0000"));
        securitiesAccount = accountRepository.save(securitiesAccount);
        BigDecimal securitiesBalanceBefore = securitiesAccount.getBalance();
        StockOrder stockOrder = stockOrderRepository.save(
                new StockOrder(
                        templateOrder.getCustomerId(),
                        templateOrder.getAccountId(),
                        "ORD-IT-" + requestSuffix,
                        "TSLA-" + requestSuffix,
                        templateOrder.getMarket(),
                        StockOrderSide.BUY,
                        new BigDecimal("10.0000"),
                        new BigDecimal("240.5000"),
                        new BigDecimal("2405.0000"),
                        templateOrder.getCurrency(),
                        StockOrderStatus.PENDING_APPROVAL
                )
        );
        ApprovalRequest stockApproval = approvalRequestRepository.save(
                new ApprovalRequest(
                        ApprovalTargetType.STOCK_ORDER,
                        stockOrder.getId(),
                        "Stock order " + stockOrder.getOrderNumber(),
                        "Stock integration approval",
                        ApprovalStatus.PENDING,
                        "approval-test@mvpbanking.local"
                )
        );

        mockMvc.perform(post("/api/admin/approvals/{approvalId}/approve", stockApproval.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "Order filled"
                                }
                                """))
                .andExpect(status().isOk());

        StockOrder approvedOrder = stockOrderRepository.findById(stockOrder.getId()).orElseThrow();
        Account debitedSecuritiesAccount = accountRepository.findById(securitiesAccount.getId()).orElseThrow();
        var settlementTransaction = transactionEntryRepository.findByTransactionNumber(approvedOrder.getSettlementTransactionNumber()).orElseThrow();

        assertThat(approvedOrder.getStatus()).isEqualTo(StockOrderStatus.PARTIALLY_FILLED);
        assertThat(approvedOrder.getExecutedQuantity()).isLessThan(stockOrder.getQuantity());
        assertThat(approvedOrder.getRemainingQuantity()).isGreaterThan(BigDecimal.ZERO);
        assertThat(approvedOrder.getFeeAmount()).isGreaterThan(BigDecimal.ZERO);
        assertThat(approvedOrder.getTaxAmount()).isEqualByComparingTo(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
        assertThat(approvedOrder.getNetSettlementAmount()).isEqualByComparingTo(settlementTransaction.getAmount());
        assertThat(approvedOrder.getExecutedPrice()).isLessThanOrEqualTo(stockOrder.getLimitPrice());
        assertThat(approvedOrder.getSettledAt()).isNotNull();
        assertThat(debitedSecuritiesAccount.getBalance()).isEqualByComparingTo(securitiesBalanceBefore.subtract(settlementTransaction.getAmount()));
        assertThat(stockOrderExecutionRepository.findByOrderIdOrderByExecutedAtDesc(approvedOrder.getId())).hasSize(2);

        mockMvc.perform(post("/api/admin/stock-orders/{orderId}/complete-fill", approvedOrder.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        StockOrder completedOrder = stockOrderRepository.findById(stockOrder.getId()).orElseThrow();
        Account completedSecuritiesAccount = accountRepository.findById(securitiesAccount.getId()).orElseThrow();
        var updatedSettlementTransaction = transactionEntryRepository.findByTransactionNumber(completedOrder.getSettlementTransactionNumber()).orElseThrow();

        assertThat(completedOrder.getStatus()).isEqualTo(StockOrderStatus.APPROVED);
        assertThat(completedOrder.getExecutedQuantity()).isEqualByComparingTo(stockOrder.getQuantity());
        assertThat(completedOrder.getRemainingQuantity()).isEqualByComparingTo(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
        assertThat(completedOrder.getFeeAmount()).isGreaterThan(approvedOrder.getFeeAmount());
        assertThat(completedOrder.getNetSettlementAmount()).isEqualByComparingTo(updatedSettlementTransaction.getAmount());
        assertThat(completedOrder.getExecutedPrice()).isLessThanOrEqualTo(stockOrder.getLimitPrice());
        assertThat(completedSecuritiesAccount.getBalance()).isEqualByComparingTo(securitiesBalanceBefore.subtract(updatedSettlementTransaction.getAmount()));
        assertThat(stockOrderExecutionRepository.findByOrderIdOrderByExecutedAtDesc(completedOrder.getId())).hasSize(3);
        assertThat(stockPositionRepository.findByAccountIdAndSymbolIgnoreCase(stockOrder.getAccountId(), stockOrder.getSymbol()))
                .isPresent()
                .get()
                .extracting(position -> position.getQuantity())
                .isEqualTo(stockOrder.getQuantity());
    }

    private String loginAsAdmin() throws Exception {
        String content = mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@mvpbanking.local",
                                  "password": "Admin1234!"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(content);
        return root.path("data").path("accessToken").asText();
    }
}
