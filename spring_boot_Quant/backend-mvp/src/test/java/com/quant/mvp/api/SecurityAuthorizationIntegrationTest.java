package com.quant.mvp.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
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
class SecurityAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void protectedApiWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void adminTokenCanCallProtectedApi() throws Exception {
        String adminToken = loginAndGetToken("admin@quant.io", "demo1234");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void traderTokenIsForbiddenForUserRead() throws Exception {
        String traderToken = loginAndGetToken("trader@quant.io", "trader1234");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + traderToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void viewerCannotDeleteOrder() throws Exception {
        String adminToken = loginAndGetToken("admin@quant.io", "demo1234");
        String viewerToken = loginAndGetToken("viewer@quant.io", "viewer1234");

        MvcResult orderResult = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "symbol": "MSFT",
                                  "side": "BUY",
                                  "orderType": "MARKET",
                                  "timeInForce": "DAY",
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode orderBody = objectMapper.readTree(orderResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
        long orderId = orderBody.get("orderId").asLong();

        mockMvc.perform(delete("/api/orders/{orderId}", orderId)
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void traderCannotCancelOrder() throws Exception {
        String adminToken = loginAndGetToken("admin@quant.io", "demo1234");
        String traderToken = loginAndGetToken("trader@quant.io", "trader1234");

        MvcResult orderResult = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "symbol": "AMD",
                                  "side": "BUY",
                                  "orderType": "LIMIT",
                                  "timeInForce": "DAY",
                                  "limitPrice": 210.25,
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode orderBody = objectMapper.readTree(orderResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
        long orderId = orderBody.get("orderId").asLong();

        mockMvc.perform(post("/api/orders/{orderId}/cancel", orderId)
                        .header("Authorization", "Bearer " + traderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "trader cannot cancel"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void viewerCannotReadOrderAudits() throws Exception {
        String viewerToken = loginAndGetToken("viewer@quant.io", "viewer1234");

        mockMvc.perform(get("/api/orders/audit-logs")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void viewerCannotReadOrderAuditSummary() throws Exception {
        String viewerToken = loginAndGetToken("viewer@quant.io", "viewer1234");

        mockMvc.perform(get("/api/orders/audit-logs/summary")
                        .header("Authorization", "Bearer " + viewerToken)
                        .param("portfolioId", "1")
                        .param("recentMinutes", "180"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void auditLogsExposeActorAndSupportActorFilter() throws Exception {
        String adminToken = loginAndGetToken("admin@quant.io", "demo1234");

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "symbol": "INTC",
                                  "side": "BUY",
                                  "orderType": "MARKET",
                                  "timeInForce": "DAY",
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/orders/audit-logs")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("actor", "admin@quant.io"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].actor").value("admin@quant.io"));
    }

    @Test
    void auditSummaryProvidesActionAndTransitionCounters() throws Exception {
        String adminToken = loginAndGetToken("admin@quant.io", "demo1234");
        String symbol = "SUM" + (System.currentTimeMillis() % 100000);

        MvcResult orderResult = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "symbol": "%s",
                                  "side": "BUY",
                                  "orderType": "MARKET",
                                  "timeInForce": "DAY",
                                  "quantity": 1
                                }
                                """.formatted(symbol)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode orderBody = objectMapper.readTree(orderResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
        long orderId = orderBody.get("orderId").asLong();

        mockMvc.perform(post("/api/orders/{orderId}/cancel", orderId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "summary validation"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/orders/audit-logs/summary")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("portfolioId", "1")
                        .param("symbol", symbol)
                        .param("recentMinutes", "180"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(2))
                .andExpect(jsonPath("$.recentCount").value(2))
                .andExpect(jsonPath("$.distinctOrderCount").value(1))
                .andExpect(jsonPath("$.actionCounters[0].action").value("CANCEL"))
                .andExpect(jsonPath("$.transitionCounters").isArray())
                .andExpect(jsonPath("$.topActors[0].actor").value("admin@quant.io"));
    }

    @Test
    void adminCanFilterOrdersWithAdvancedCriteria() throws Exception {
        String adminToken = loginAndGetToken("admin@quant.io", "demo1234");
        String symbol = "FLT" + (System.currentTimeMillis() % 100000);

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "symbol": "%s",
                                  "side": "BUY",
                                  "orderType": "LIMIT",
                                  "timeInForce": "DAY",
                                  "limitPrice": 132.25,
                                  "quantity": 3
                                }
                                """.formatted(symbol)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("portfolioId", "1")
                        .param("symbol", symbol)
                        .param("status", "NEW")
                        .param("side", "BUY")
                        .param("orderType", "LIMIT")
                        .param("timeInForce", "DAY")
                        .param("minQuantity", "3")
                        .param("maxQuantity", "3")
                        .param("minRemainingQuantity", "3")
                        .param("maxRemainingQuantity", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].symbol").value(symbol))
                .andExpect(jsonPath("$.items[0].side").value("BUY"))
                .andExpect(jsonPath("$.items[0].orderType").value("LIMIT"))
                .andExpect(jsonPath("$.items[0].timeInForce").value("DAY"));
    }

    @Test
    void adminCanReadPortfolioSummaryInsight() throws Exception {
        String adminToken = loginAndGetToken("admin@quant.io", "demo1234");

        mockMvc.perform(get("/api/portfolio-summaries/insight")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("portfolioId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].portfolioId").value(1))
                .andExpect(jsonPath("$.items[0].healthScore").isNumber())
                .andExpect(jsonPath("$.items[0].healthStatus").isString())
                .andExpect(jsonPath("$.items[0].topExposures").isArray());
    }

    @Test
    void adminCanReadPortfolioProfitPlaybook() throws Exception {
        String adminToken = loginAndGetToken("admin@quant.io", "demo1234");

        mockMvc.perform(get("/api/portfolio-summaries/profit-playbook")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("portfolioId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].portfolioId").value(1))
                .andExpect(jsonPath("$.items[0].objective").isString())
                .andExpect(jsonPath("$.items[0].actions").isArray());
    }

    @Test
    void adminCanReadPortfolioProfitPlaybookFeedback() throws Exception {
        String adminToken = loginAndGetToken("admin@quant.io", "demo1234");

        mockMvc.perform(post("/api/account/work-queue/actions/remediate-stale-orders")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "staleMinutes": 0,
                                  "reason": "feedback integration test"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/portfolio-summaries/profit-playbook/feedback")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("portfolioId", "1")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].actionKey").isString())
                .andExpect(jsonPath("$.items[0].beforeSnapshot").exists())
                .andExpect(jsonPath("$.items[0].afterSnapshot").exists())
                .andExpect(jsonPath("$.items[0].outcomeEvaluation").isString());
    }

    @Test
    void adminCanReadRiskAlertOverview() throws Exception {
        String adminToken = loginAndGetToken("admin@quant.io", "demo1234");

        mockMvc.perform(get("/api/risk-alerts/overview")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("portfolioId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].portfolioId").value(1))
                .andExpect(jsonPath("$.items[0].totalCount").isNumber())
                .andExpect(jsonPath("$.items[0].criticalCount").isNumber())
                .andExpect(jsonPath("$.items[0].slaBreachedCount").isNumber());
    }

    @Test
    void riskCanToggleKillSwitchAndOrderCreationIsBlocked() throws Exception {
        String adminToken = loginAndGetToken("admin@quant.io", "demo1234");
        String riskToken = loginAndGetToken("risk@quant.io", "risk1234");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/risk-limits/trading-controls")
                        .header("Authorization", "Bearer " + riskToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "tradingEnabled": false,
                                  "reason": "circuit breaker"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].portfolioId").value(1))
                .andExpect(jsonPath("$.items[0].tradingEnabled").value(false))
                .andExpect(jsonPath("$.items[0].updatedBy").value("risk@quant.io"));

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "symbol": "AMD",
                                  "side": "BUY",
                                  "orderType": "MARKET",
                                  "timeInForce": "DAY",
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));

        MvcResult riskAlertsResult = mockMvc.perform(get("/api/risk-alerts")
                        .header("Authorization", "Bearer " + riskToken)
                        .param("portfolioId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andReturn();

        JsonNode riskAlertItems = objectMapper
                .readTree(riskAlertsResult.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .path("items");
        String tradingHaltedAlertKey = null;
        for (JsonNode item : riskAlertItems) {
            if ("TRADING_HALTED".equals(item.path("code").asText(""))) {
                tradingHaltedAlertKey = item.path("alertKey").asText();
                break;
            }
        }
        assertTrue(tradingHaltedAlertKey != null && !tradingHaltedAlertKey.isBlank(), "trading halted alert key should exist");

        mockMvc.perform(post("/api/risk-alerts/ack")
                        .header("Authorization", "Bearer " + riskToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "alertKey": "%s",
                                  "note": "risk acknowledged"
                                }
                                """.formatted(tradingHaltedAlertKey)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.acknowledged").value(true))
                .andExpect(jsonPath("$.item.acknowledgedBy").value("risk@quant.io"));

        mockMvc.perform(post("/api/risk-alerts/workflow")
                        .header("Authorization", "Bearer " + riskToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "alertKey": "%s",
                                  "workflowStatus": "IN_PROGRESS",
                                  "note": "triage started",
                                  "assignee": "risk@quant.io"
                                }
                                """.formatted(tradingHaltedAlertKey)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.workflowStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.item.assignee").value("risk@quant.io"));

        mockMvc.perform(post("/api/risk-alerts/workflow")
                        .header("Authorization", "Bearer " + riskToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "alertKey": "%s",
                                  "workflowStatus": "RESOLVED",
                                  "note": "resolved by risk"
                                }
                                """.formatted(tradingHaltedAlertKey)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.workflowStatus").value("RESOLVED"))
                .andExpect(jsonPath("$.item.resolvedBy").value("risk@quant.io"));

        mockMvc.perform(post("/api/risk-alerts/unack")
                        .header("Authorization", "Bearer " + riskToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "alertKey": "%s"
                                }
                                """.formatted(tradingHaltedAlertKey)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.acknowledged").value(false))
                .andExpect(jsonPath("$.item.workflowStatus").value("OPEN"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/risk-limits/trading-controls")
                        .header("Authorization", "Bearer " + riskToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "tradingEnabled": true,
                                  "reason": "resume"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].tradingEnabled").value(true));

        MvcResult historyResult = mockMvc.perform(get("/api/risk-limits/trading-controls/history")
                        .header("Authorization", "Bearer " + riskToken)
                        .param("portfolioId", "1")
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andReturn();

        JsonNode historyItems = objectMapper
                .readTree(historyResult.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .path("items");
        boolean hasDisable = false;
        boolean hasEnable = false;
        for (JsonNode item : historyItems) {
            String action = item.path("action").asText("");
            String reason = item.path("reason").asText("");
            if ("DISABLE".equals(action) && "circuit breaker".equals(reason)) {
                hasDisable = true;
            }
            if ("ENABLE".equals(action) && "resume".equals(reason)) {
                hasEnable = true;
            }
        }
        assertTrue(hasDisable, "trading control history should contain disable event");
        assertTrue(hasEnable, "trading control history should contain enable event");
    }

    @Test
    void viewerCanReadRiskAlerts() throws Exception {
        String viewerToken = loginAndGetToken("viewer@quant.io", "viewer1234");

        mockMvc.perform(get("/api/risk-alerts")
                        .header("Authorization", "Bearer " + viewerToken)
                        .param("portfolioId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void viewerCannotAcknowledgeRiskAlerts() throws Exception {
        String viewerToken = loginAndGetToken("viewer@quant.io", "viewer1234");

        mockMvc.perform(post("/api/risk-alerts/ack")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "alertKey": "RISK_OK_1",
                                  "note": "viewer forbidden"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        mockMvc.perform(post("/api/risk-alerts/workflow")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "alertKey": "TRADING_HALTED",
                                  "workflowStatus": "IN_PROGRESS",
                                  "note": "viewer forbidden",
                                  "assignee": "viewer@quant.io"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void viewerCanReadExecutionQualities() throws Exception {
        String viewerToken = loginAndGetToken("viewer@quant.io", "viewer1234");

        mockMvc.perform(get("/api/execution-qualities")
                        .header("Authorization", "Bearer " + viewerToken)
                        .param("portfolioId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void viewerCanReadPortfolioCatalog() throws Exception {
        String viewerToken = loginAndGetToken("viewer@quant.io", "viewer1234");

        mockMvc.perform(get("/api/portfolios")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void viewerCanReadOrderHealth() throws Exception {
        String viewerToken = loginAndGetToken("viewer@quant.io", "viewer1234");

        mockMvc.perform(get("/api/order-health")
                        .header("Authorization", "Bearer " + viewerToken)
                        .param("portfolioId", "1")
                        .param("staleMinutes", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void viewerCannotRemediateStaleOrders() throws Exception {
        String viewerToken = loginAndGetToken("viewer@quant.io", "viewer1234");

        mockMvc.perform(post("/api/order-health/remediate-stale")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "symbol": "IWM",
                                  "staleMinutes": 0,
                                  "reason": "viewer forbidden"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void viewerCanReadWorkQueueAndGetsReadOnlyStaleOrderTask() throws Exception {
        String adminToken = loginAndGetToken("admin@quant.io", "demo1234");
        String viewerToken = loginAndGetToken("viewer@quant.io", "viewer1234");

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "symbol": "QQQ",
                                  "side": "BUY",
                                  "orderType": "LIMIT",
                                  "timeInForce": "DAY",
                                  "limitPrice": 520.25,
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/api/account/work-queue")
                        .header("Authorization", "Bearer " + viewerToken)
                        .param("portfolioId", "1")
                        .param("staleMinutes", "0")
                        .param("topN", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.portfolioId").value(1))
                .andExpect(jsonPath("$.tasks").isArray())
                .andReturn();

        JsonNode tasks = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .path("tasks");
        boolean hasStaleTask = false;
        boolean staleActionEnabled = false;
        for (JsonNode task : tasks) {
            if ("staleOrders".equals(task.path("taskKey").asText())) {
                hasStaleTask = true;
                staleActionEnabled = task.path("actionEnabled").asBoolean();
                break;
            }
        }
        assertTrue(hasStaleTask);
        assertFalse(staleActionEnabled);
    }

    @Test
    void viewerCannotExecuteWorkQueueActions() throws Exception {
        String viewerToken = loginAndGetToken("viewer@quant.io", "viewer1234");

        mockMvc.perform(post("/api/account/work-queue/actions/remediate-stale-orders")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "staleMinutes": 0,
                                  "reason": "viewer forbidden"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        mockMvc.perform(post("/api/account/work-queue/actions/revoke-other-sessions")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "viewer forbidden"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        mockMvc.perform(post("/api/account/work-queue/actions/post-approved-vouchers")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "limit": 5,
                                  "reason": "viewer forbidden"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        mockMvc.perform(post("/api/account/work-queue/actions/approve-draft-vouchers")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "limit": 5,
                                  "reason": "viewer forbidden"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        mockMvc.perform(post("/api/account/work-queue/actions/pause-trading")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "reason": "viewer forbidden"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        mockMvc.perform(post("/api/account/work-queue/actions/emergency-risk-response")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "reason": "viewer forbidden",
                                  "cancelOpenOrders": true
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        mockMvc.perform(post("/api/account/work-queue/actions/resume-trading")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "reason": "viewer forbidden",
                                  "force": false
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void adminCanExecuteWorkQueueActions() throws Exception {
        String adminToken = loginAndGetToken("admin@quant.io", "demo1234");

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "symbol": "WQMS",
                                  "side": "BUY",
                                  "orderType": "LIMIT",
                                  "timeInForce": "DAY",
                                  "limitPrice": 111.25,
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/account/work-queue/actions/remediate-stale-orders")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "staleMinutes": 0,
                                  "reason": "admin remediation"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portfolioId").value(1))
                .andExpect(jsonPath("$.canceledCount").isNumber());

        mockMvc.perform(post("/api/account/work-queue/actions/revoke-other-sessions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "admin hardening"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revokedCount").isNumber())
                .andExpect(jsonPath("$.revokedSessionIds").isArray());

        mockMvc.perform(post("/api/account/work-queue/actions/post-approved-vouchers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "limit": 20,
                                  "reason": "admin voucher posting"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portfolioId").value(1))
                .andExpect(jsonPath("$.attemptedCount").isNumber())
                .andExpect(jsonPath("$.postedCount").isNumber())
                .andExpect(jsonPath("$.postedVoucherIds").isArray())
                .andExpect(jsonPath("$.failedReasons").isArray());

        mockMvc.perform(post("/api/account/work-queue/actions/approve-draft-vouchers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "limit": 20,
                                  "reason": "admin voucher approval"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portfolioId").value(1))
                .andExpect(jsonPath("$.attemptedCount").isNumber())
                .andExpect(jsonPath("$.approvedCount").isNumber())
                .andExpect(jsonPath("$.approvedVoucherIds").isArray())
                .andExpect(jsonPath("$.failedReasons").isArray());

        mockMvc.perform(post("/api/account/work-queue/actions/pause-trading")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "reason": "admin critical risk response"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portfolioId").value(1))
                .andExpect(jsonPath("$.tradingEnabled").value(false))
                .andExpect(jsonPath("$.killSwitchReason").value("admin critical risk response"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/risk-limits/trading-controls")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "tradingEnabled": true,
                                  "reason": "restore before emergency test"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].tradingEnabled").value(true));

        MvcResult emergencyOrderResult = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "symbol": "EMRG",
                                  "side": "BUY",
                                  "orderType": "LIMIT",
                                  "timeInForce": "DAY",
                                  "limitPrice": 130.10,
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        long emergencyOrderId = objectMapper.readTree(emergencyOrderResult.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .get("orderId")
                .asLong();

        mockMvc.perform(post("/api/account/work-queue/actions/emergency-risk-response")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "reason": "admin emergency action",
                                  "cancelOpenOrders": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portfolioId").value(1))
                .andExpect(jsonPath("$.tradingEnabled").value(false))
                .andExpect(jsonPath("$.canceledCount").isNumber())
                .andExpect(jsonPath("$.canceledOrderIds").isArray());

        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("portfolioId", "1")
                        .param("symbol", "EMRG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].orderId").value(emergencyOrderId))
                .andExpect(jsonPath("$.items[0].status").value("CANCELED"));

        mockMvc.perform(post("/api/account/work-queue/actions/resume-trading")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "reason": "resume after emergency",
                                  "force": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portfolioId").value(1))
                .andExpect(jsonPath("$.tradingEnabled").value(true))
                .andExpect(jsonPath("$.resumed").value(true))
                .andExpect(jsonPath("$.blockedCriticalCount").value(0))
                .andExpect(jsonPath("$.blockedCodes").isArray())
                .andExpect(jsonPath("$.blockedMessages").isArray());
    }

    @Test
    void viewerCanReadActivityFeed() throws Exception {
        String viewerToken = loginAndGetToken("viewer@quant.io", "viewer1234");

        mockMvc.perform(get("/api/account/activity-feed")
                        .header("Authorization", "Bearer " + viewerToken)
                        .param("portfolioId", "1")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.req.portfolioId").value(1))
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void riskCanRemediateStaleOrders() throws Exception {
        String adminToken = loginAndGetToken("admin@quant.io", "demo1234");
        String riskToken = loginAndGetToken("risk@quant.io", "risk1234");

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "symbol": "IWM",
                                  "side": "BUY",
                                  "orderType": "LIMIT",
                                  "timeInForce": "DAY",
                                  "limitPrice": 204.10,
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/order-health/remediate-stale")
                        .header("Authorization", "Bearer " + riskToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "symbol": "IWM",
                                  "staleMinutes": 0,
                                  "reason": "risk remediation test"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canceledCount").isNumber())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void traderCanReadOrderWorkbenchSummary() throws Exception {
        String traderToken = loginAndGetToken("trader@quant.io", "trader1234");

        mockMvc.perform(get("/api/orders/workbench")
                        .header("Authorization", "Bearer " + traderToken)
                        .param("portfolioId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.portfolioId").value(1))
                .andExpect(jsonPath("$.statusCounters").isArray());
    }

    @Test
    void viewerCanReadSavedViewsButCannotCreate() throws Exception {
        String viewerToken = loginAndGetToken("viewer@quant.io", "viewer1234");

        mockMvc.perform(get("/api/saved-views")
                        .header("Authorization", "Bearer " + viewerToken)
                        .param("resourceKey", "orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());

        mockMvc.perform(post("/api/saved-views")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resourceKey": "orders",
                                  "viewName": "viewer view",
                                  "description": "forbidden",
                                  "shared": false,
                                  "filters": {
                                    "status": "NEW"
                                  }
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void traderCanCreateAndDeleteOwnSavedView() throws Exception {
        String traderToken = loginAndGetToken("trader@quant.io", "trader1234");

        MvcResult create = mockMvc.perform(post("/api/saved-views")
                        .header("Authorization", "Bearer " + traderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resourceKey": "orders",
                                  "viewName": "My Desk View",
                                  "description": "trader scoped view",
                                  "shared": false,
                                  "filters": {
                                    "portfolioId": "1",
                                    "symbol": "MSFT",
                                    "status": "PARTIAL"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceKey").value("orders"))
                .andExpect(jsonPath("$.ownerEmail").value("trader@quant.io"))
                .andReturn();

        long viewId = objectMapper.readTree(create.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .get("viewId")
                .asLong();

        mockMvc.perform(delete("/api/saved-views/{viewId}", viewId)
                        .header("Authorization", "Bearer " + traderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viewId").value(viewId));
    }

    @Test
    void viewerCanPinAndReadDefaultSavedView() throws Exception {
        String viewerToken = loginAndGetToken("viewer@quant.io", "viewer1234");

        mockMvc.perform(post("/api/saved-views/default")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resourceKey": "orders",
                                  "viewId": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viewId").value(1))
                .andExpect(jsonPath("$.resourceKey").value("orders"));

        mockMvc.perform(get("/api/saved-views/default")
                        .header("Authorization", "Bearer " + viewerToken)
                        .param("resourceKey", "orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viewId").value(1))
                .andExpect(jsonPath("$.viewName").isNotEmpty());
    }

    @Test
    void viewerCannotPinInaccessiblePrivateView() throws Exception {
        String viewerToken = loginAndGetToken("viewer@quant.io", "viewer1234");

        mockMvc.perform(post("/api/saved-views/default")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resourceKey": "orders",
                                  "viewId": 3
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void traderCannotCreateSharedSavedView() throws Exception {
        String traderToken = loginAndGetToken("trader@quant.io", "trader1234");

        mockMvc.perform(post("/api/saved-views")
                        .header("Authorization", "Bearer " + traderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resourceKey": "orders",
                                  "viewName": "shared-by-trader",
                                  "description": "must be forbidden",
                                  "shared": true,
                                  "filters": {
                                    "status": "NEW"
                                  }
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void traderCannotDeleteAnotherUsersSavedView() throws Exception {
        String adminToken = loginAndGetToken("admin@quant.io", "demo1234");
        String traderToken = loginAndGetToken("trader@quant.io", "trader1234");

        MvcResult create = mockMvc.perform(post("/api/saved-views")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resourceKey": "orders",
                                  "viewName": "admin-owned-view",
                                  "description": "owner test",
                                  "shared": true,
                                  "filters": {
                                    "status": "NEW"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        long viewId = objectMapper.readTree(create.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .get("viewId")
                .asLong();

        mockMvc.perform(delete("/api/saved-views/{viewId}", viewId)
                        .header("Authorization", "Bearer " + traderToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void adminCanBulkCancelOrders() throws Exception {
        String adminToken = loginAndGetToken("admin@quant.io", "demo1234");

        MvcResult firstOrderResult = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "symbol": "QCOM",
                                  "side": "BUY",
                                  "orderType": "LIMIT",
                                  "timeInForce": "DAY",
                                  "limitPrice": 155.10,
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();
        long firstOrderId = objectMapper.readTree(firstOrderResult.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .get("orderId")
                .asLong();

        MvcResult secondOrderResult = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "symbol": "ORCL",
                                  "side": "BUY",
                                  "orderType": "LIMIT",
                                  "timeInForce": "DAY",
                                  "limitPrice": 102.35,
                                  "quantity": 3
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();
        long secondOrderId = objectMapper.readTree(secondOrderResult.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .get("orderId")
                .asLong();

        mockMvc.perform(post("/api/orders/bulk/cancel")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderIds": [%d, %d],
                                  "reason": "bulk cancel test"
                                }
                                """.formatted(firstOrderId, secondOrderId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("CANCEL"))
                .andExpect(jsonPath("$.requestedCount").value(2))
                .andExpect(jsonPath("$.successCount").value(2))
                .andExpect(jsonPath("$.failedCount").value(0));
    }

    @Test
    void traderCannotBulkRejectOrders() throws Exception {
        String traderToken = loginAndGetToken("trader@quant.io", "trader1234");

        mockMvc.perform(post("/api/orders/bulk/reject")
                        .header("Authorization", "Bearer " + traderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderIds": [1001, 1002],
                                  "reason": "forbidden"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void traderCanReadOrderInsight() throws Exception {
        String adminToken = loginAndGetToken("admin@quant.io", "demo1234");
        String traderToken = loginAndGetToken("trader@quant.io", "trader1234");

        MvcResult orderResult = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "symbol": "SHOP",
                                  "side": "BUY",
                                  "orderType": "LIMIT",
                                  "timeInForce": "DAY",
                                  "limitPrice": 88.40,
                                  "quantity": 3
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        long orderId = objectMapper.readTree(orderResult.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .get("orderId")
                .asLong();

        mockMvc.perform(post("/api/trades/events")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": %d,
                                  "tradeQuantity": 1,
                                  "tradePrice": 88.40
                                }
                                """.formatted(orderId)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/orders/{orderId}/insight", orderId)
                        .header("Authorization", "Bearer " + traderToken)
                        .param("staleMinutes", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.symbol").value("SHOP"))
                .andExpect(jsonPath("$.trades").isArray())
                .andExpect(jsonPath("$.audits").isArray())
                .andExpect(jsonPath("$.riskAlerts").isArray());
    }

    @Test
    void viewerCanReadGlobalSearchButCannotSeeOrdersSection() throws Exception {
        String viewerToken = loginAndGetToken("viewer@quant.io", "viewer1234");

        MvcResult searchResult = mockMvc.perform(get("/api/search/global")
                        .header("Authorization", "Bearer " + viewerToken)
                        .param("q", "order"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sections").isArray())
                .andReturn();

        JsonNode sections = objectMapper.readTree(searchResult.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .path("sections");
        boolean hasOrdersSection = false;
        for (JsonNode section : sections) {
            if ("orders".equalsIgnoreCase(section.path("resourceKey").asText())) {
                hasOrdersSection = true;
                break;
            }
        }
        assertFalse(hasOrdersSection);
    }

    @Test
    void adminGlobalSearchIncludesOrdersSection() throws Exception {
        String adminToken = loginAndGetToken("admin@quant.io", "demo1234");

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 1,
                                  "symbol": "NVDA",
                                  "side": "BUY",
                                  "orderType": "LIMIT",
                                  "timeInForce": "DAY",
                                  "limitPrice": 730.0,
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isOk());

        MvcResult searchResult = mockMvc.perform(get("/api/search/global")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("q", "nvda"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sections").isArray())
                .andReturn();

        JsonNode sections = objectMapper.readTree(searchResult.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .path("sections");
        boolean hasOrdersSection = false;
        for (JsonNode section : sections) {
            if ("orders".equalsIgnoreCase(section.path("resourceKey").asText())) {
                hasOrdersSection = true;
                break;
            }
        }
        assertTrue(hasOrdersSection);
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();

        JsonNode body = objectMapper.readTree(loginResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
        return body.get("accessToken").asText();
    }
}
