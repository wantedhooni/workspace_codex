package com.quant.mvp.pipeline.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.quant.mvp.pipeline.domain.Order;
import com.quant.mvp.pipeline.domain.OrderSide;
import com.quant.mvp.pipeline.domain.OrderStatus;
import com.quant.mvp.pipeline.domain.OrderType;
import com.quant.mvp.pipeline.domain.Position;
import com.quant.mvp.pipeline.domain.TimeInForce;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderTradePositionPipelineServiceTest {

    @Test
    void sellOrderCannotExceedAvailablePosition() {
        OrderTradePositionPipelineService service = new OrderTradePositionPipelineService();

        Order buyOrder = service.createOrder(1L, "AAPL", OrderSide.BUY, new BigDecimal("10"));
        service.applyTrade(buyOrder.orderId(), new BigDecimal("10"), new BigDecimal("180.00"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createOrder(1L, "AAPL", OrderSide.SELL, new BigDecimal("11"))
        );
    }

    @Test
    void tradeCannotExceedRemainingOrderQuantity() {
        OrderTradePositionPipelineService service = new OrderTradePositionPipelineService();

        Order order = service.createOrder(1L, "MSFT", OrderSide.BUY, new BigDecimal("5"));
        service.applyTrade(order.orderId(), new BigDecimal("3"), new BigDecimal("410.00"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.applyTrade(order.orderId(), new BigDecimal("3"), new BigDecimal("411.00"))
        );
    }

    @Test
    void positionsAreSeparatedByPortfolio() {
        OrderTradePositionPipelineService service = new OrderTradePositionPipelineService();

        Order p1 = service.createOrder(1L, "TSLA", OrderSide.BUY, new BigDecimal("2"));
        service.applyTrade(p1.orderId(), new BigDecimal("2"), new BigDecimal("200.00"));

        Order p2 = service.createOrder(2L, "TSLA", OrderSide.BUY, new BigDecimal("3"));
        service.applyTrade(p2.orderId(), new BigDecimal("3"), new BigDecimal("210.00"));

        List<Position> p1Positions = service.getPositions(1L);
        List<Position> p2Positions = service.getPositions(2L);

        assertEquals(1, p1Positions.size());
        assertEquals(1, p2Positions.size());
        assertEquals(0, p1Positions.get(0).quantity().compareTo(new BigDecimal("2.000000")));
        assertEquals(0, p2Positions.get(0).quantity().compareTo(new BigDecimal("3.000000")));
    }

    @Test
    void realizedAndUnrealizedPnlIncludeFeeAndSlippage() {
        OrderTradePositionPipelineService service = new OrderTradePositionPipelineService();

        Order buyOrder = service.createOrder(1L, "QQQ", OrderSide.BUY, new BigDecimal("10"));
        service.applyTrade(buyOrder.orderId(), new BigDecimal("10"), new BigDecimal("100.00"));

        Order sellOrder = service.createOrder(1L, "QQQ", OrderSide.SELL, new BigDecimal("4"));
        service.applyTrade(sellOrder.orderId(), new BigDecimal("4"), new BigDecimal("110.00"));

        List<Position> positions = service.getPositions(1L);
        Position position = positions.get(0);

        assertEquals(0, position.quantity().compareTo(new BigDecimal("6.000000")));
        assertEquals(0, position.realizedPnl().compareTo(new BigDecimal("39.664000")));
        assertEquals(0, position.unrealizedPnl().compareTo(new BigDecimal("59.760000")));
    }

    @Test
    void limitBuyCannotExecuteAboveLimitPrice() {
        OrderTradePositionPipelineService service = new OrderTradePositionPipelineService();

        Order order = service.createOrder(
                1L,
                "AAPL",
                OrderSide.BUY,
                new BigDecimal("10"),
                OrderType.LIMIT,
                TimeInForce.DAY,
                new BigDecimal("180")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.applyTrade(order.orderId(), new BigDecimal("1"), new BigDecimal("181"))
        );
    }

    @Test
    void perSymbolOpenOrderLimitIsEnforced() {
        OrderTradePositionPipelineService service = new OrderTradePositionPipelineService();

        service.upsertRiskLimit(
                1L,
                new BigDecimal("5000000"),
                new BigDecimal("5000000"),
                new BigDecimal("10000000"),
                2,
                new BigDecimal("0"),
                new BigDecimal("0")
        );

        service.createOrder(1L, "AAPL", OrderSide.BUY, new BigDecimal("1"));
        service.createOrder(1L, "AAPL", OrderSide.BUY, new BigDecimal("1"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createOrder(1L, "AAPL", OrderSide.BUY, new BigDecimal("1"))
        );
    }

    @Test
    void deleteOrderRequiresNoExecution() {
        OrderTradePositionPipelineService service = new OrderTradePositionPipelineService();

        Order newOrder = service.createOrder(1L, "NFLX", OrderSide.BUY, new BigDecimal("2"));
        Order deleted = service.deleteOrder(newOrder.orderId());
        assertEquals(newOrder.orderId(), deleted.orderId());

        Order executed = service.createOrder(1L, "META", OrderSide.BUY, new BigDecimal("1"));
        service.applyTrade(executed.orderId(), new BigDecimal("1"), new BigDecimal("300"));
        assertThrows(IllegalArgumentException.class, () -> service.deleteOrder(executed.orderId()));
    }

    @Test
    void cancelOrderChangesStatusToCanceled() {
        OrderTradePositionPipelineService service = new OrderTradePositionPipelineService();

        Order order = service.createOrder(1L, "ORCL", OrderSide.BUY, new BigDecimal("5"));
        Order canceled = service.cancelOrder(order.orderId());

        assertEquals(OrderStatus.CANCELED, canceled.status());
    }

    @Test
    void rejectOrderRequiresUnfilledOrder() {
        OrderTradePositionPipelineService service = new OrderTradePositionPipelineService();

        Order order = service.createOrder(1L, "IBM", OrderSide.BUY, new BigDecimal("2"));
        service.applyTrade(order.orderId(), new BigDecimal("1"), new BigDecimal("150"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.rejectOrder(order.orderId(), "risk reject")
        );
    }

    @Test
    void orderAuditLogsIncludeCreateTradeAndDecision() {
        OrderTradePositionPipelineService service = new OrderTradePositionPipelineService();

        Order order = service.createOrder(
                1L,
                "CRM",
                OrderSide.BUY,
                new BigDecimal("3"),
                OrderType.MARKET,
                TimeInForce.DAY,
                null,
                "admin@quant.io"
        );
        service.applyTrade(order.orderId(), new BigDecimal("1"), new BigDecimal("250"), "trader@quant.io");
        service.cancelOrder(order.orderId(), "ops cancel", "risk@quant.io");

        var audits = service.searchOrderAudits(order.orderId(), null, null, null);
        assertEquals(3, audits.size());
        assertEquals("CREATE", audits.get(0).action());
        assertEquals("admin@quant.io", audits.get(0).actor());
        assertEquals("TRADE_APPLIED", audits.get(1).action());
        assertEquals("trader@quant.io", audits.get(1).actor());
        assertEquals("CANCEL", audits.get(2).action());
        assertEquals("ops cancel", audits.get(2).reason());
        assertEquals("risk@quant.io", audits.get(2).actor());
    }

    @Test
    void orderAuditSummaryAggregatesActionTransitionAndActor() {
        OrderTradePositionPipelineService service = new OrderTradePositionPipelineService();

        Order order = service.createOrder(
                1L,
                "SHOP",
                OrderSide.BUY,
                new BigDecimal("2"),
                OrderType.MARKET,
                TimeInForce.DAY,
                null,
                "admin@quant.io"
        );
        service.cancelOrder(order.orderId(), "ops cancel", "risk@quant.io");

        var summary = service.summarizeOrderAudits(order.orderId(), 1L, "SHOP", null, null, 180);
        assertEquals(2L, summary.totalCount());
        assertEquals(2L, summary.recentCount());
        assertEquals(1L, summary.distinctOrderCount());
        assertTrue(summary.actionCounters().stream().anyMatch(item -> "CREATE".equals(item.action()) && item.count() == 1L));
        assertTrue(summary.actionCounters().stream().anyMatch(item -> "CANCEL".equals(item.action()) && item.count() == 1L));
        assertTrue(summary.transitionCounters().stream().anyMatch(item -> "NEW".equals(item.toStatus())));
        assertTrue(summary.topActors().stream().anyMatch(item -> "admin@quant.io".equals(item.actor())));
    }

    @Test
    void killSwitchBlocksOrderAndTradeExecution() {
        OrderTradePositionPipelineService service = new OrderTradePositionPipelineService();

        Order order = service.createOrder(1L, "AAPL", OrderSide.BUY, new BigDecimal("2"));
        service.updateTradingControl(1L, false, "halt due to event risk", "risk@quant.io");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createOrder(1L, "MSFT", OrderSide.BUY, new BigDecimal("1"))
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.applyTrade(order.orderId(), new BigDecimal("1"), new BigDecimal("190"))
        );

        service.updateTradingControl(1L, true, "resume", "risk@quant.io");
        service.applyTrade(order.orderId(), new BigDecimal("1"), new BigDecimal("190"));
    }

    @Test
    void riskAlertsIncludeKillSwitchCriticalSignal() {
        OrderTradePositionPipelineService service = new OrderTradePositionPipelineService();
        service.updateTradingControl(1L, false, "ops halt", "risk@quant.io");

        var alerts = service.searchRiskAlerts(1L);
        assertTrue(
                alerts.stream().anyMatch(alert ->
                        "TRADING_HALTED".equals(alert.code())
                                && "CRITICAL".equals(alert.severity())
                                && Long.valueOf(1L).equals(alert.portfolioId()))
        );
    }

    @Test
    void riskAlertOverviewAggregatesSlaAndWorkflowMetrics() {
        OrderTradePositionPipelineService service = new OrderTradePositionPipelineService();

        service.updateTradingControl(1L, false, "ops halt", "risk@quant.io");
        var alerts = service.searchRiskAlerts(1L);
        var haltedAlert = alerts.stream()
                .filter(alert -> "TRADING_HALTED".equals(alert.code()))
                .findFirst()
                .orElseThrow();

        service.acknowledgeRiskAlert(1L, haltedAlert.alertKey(), "ack for overview", "risk@quant.io");
        service.updateRiskAlertWorkflow(1L, haltedAlert.alertKey(), "IN_PROGRESS", "triage", "risk@quant.io", "risk@quant.io");

        var overviews = service.searchRiskAlertOverviews(1L);
        assertEquals(1, overviews.size());
        var overview = overviews.get(0);
        assertEquals(Long.valueOf(1L), overview.portfolioId());
        assertTrue(overview.totalCount() >= 1);
        assertTrue(overview.criticalCount() >= 1);
        assertTrue(overview.inProgressCount() >= 1);
        assertTrue(overview.unacknowledgedCount() >= 0);

        var operational = service.evaluateRiskAlertOperationalState(haltedAlert);
        assertTrue(operational.priorityScore() > 0);
        assertTrue(operational.slaTargetMinutes() > 0);
    }

    @Test
    void executionQualityIsCalculatedByPortfolioAndSymbol() {
        OrderTradePositionPipelineService service = new OrderTradePositionPipelineService();

        Order first = service.createOrder(1L, "AAPL", OrderSide.BUY, new BigDecimal("10"));
        service.applyTrade(first.orderId(), new BigDecimal("10"), new BigDecimal("180.00"));

        Order second = service.createOrder(1L, "AAPL", OrderSide.BUY, new BigDecimal("10"));
        service.applyTrade(second.orderId(), new BigDecimal("5"), new BigDecimal("181.00"));

        var rows = service.searchExecutionQualities(1L, "AAPL");
        assertEquals(1, rows.size());

        var row = rows.get(0);
        assertEquals("AAPL", row.symbol());
        assertEquals(2, row.orderCount());
        assertEquals(1, row.filledOrderCount());
        assertEquals(0, row.fillRatePct().compareTo(new BigDecimal("75.000000")));
        assertEquals("C", row.qualityGrade());
        assertEquals(0, row.averageSlippageBps().compareTo(new BigDecimal("1.500000")));
        assertTrue(row.tradeCount() >= 2);
    }

    @Test
    void orderHealthDetectsStaleOpenOrders() {
        OrderTradePositionPipelineService service = new OrderTradePositionPipelineService();

        service.createOrder(1L, "QQQ", OrderSide.BUY, new BigDecimal("10"));
        var rows = service.searchOrderHealth(1L, "QQQ", 0);

        assertEquals(1, rows.size());
        var row = rows.get(0);
        assertEquals("QQQ", row.symbol());
        assertEquals(1, row.openOrderCount());
        assertEquals(1, row.staleOrderCount());
        assertTrue(row.maxOpenAgeMinutes() >= 0);
        assertTrue(row.healthStatus().equals("WARN") || row.healthStatus().equals("CRITICAL"));
    }

    @Test
    void portfolioSummaryInsightCalculatesHealthAndConcentration() {
        OrderTradePositionPipelineService service = new OrderTradePositionPipelineService();

        Order aaplOrder = service.createOrder(1L, "AAPL", OrderSide.BUY, new BigDecimal("10"));
        service.applyTrade(aaplOrder.orderId(), new BigDecimal("10"), new BigDecimal("180.00"));

        Order msftOrder = service.createOrder(1L, "MSFT", OrderSide.BUY, new BigDecimal("2"));
        service.applyTrade(msftOrder.orderId(), new BigDecimal("2"), new BigDecimal("420.00"));

        service.updateTradingControl(1L, false, "insight test halt", "risk@quant.io");

        var insights = service.searchPortfolioSummaryInsights(1L);
        assertEquals(1, insights.size());

        var insight = insights.get(0);
        assertEquals(Long.valueOf(1L), insight.portfolioId());
        assertEquals("CRITICAL", insight.healthStatus());
        assertTrue(insight.healthScore() >= 0 && insight.healthScore() <= 100);
        assertTrue(insight.criticalAlertCount() >= 1);
        assertEquals(Boolean.FALSE, insight.tradingEnabled());
        assertEquals("AAPL", insight.topConcentrationSymbol());
        assertTrue(insight.topConcentrationWeightPct().compareTo(BigDecimal.ZERO) > 0);
        assertEquals(2, insight.positionCount());
        assertTrue(insight.topExposures().size() >= 2);
    }

    @Test
    void profitPlaybookProvidesActionableAndBlockingGuidance() {
        OrderTradePositionPipelineService service = new OrderTradePositionPipelineService();

        Order order = service.createOrder(1L, "AAPL", OrderSide.BUY, new BigDecimal("5"));
        service.applyTrade(order.orderId(), new BigDecimal("5"), new BigDecimal("185.00"));
        service.updateTradingControl(1L, false, "playbook guard test", "risk@quant.io");

        var playbooks = service.searchPortfolioProfitPlaybooks(1L);
        assertEquals(1, playbooks.size());

        var playbook = playbooks.get(0);
        assertEquals(Long.valueOf(1L), playbook.portfolioId());
        assertTrue(playbook.priorityScore() >= 0);
        assertTrue(playbook.blockerCount() >= 1);
        assertTrue(playbook.actions().stream().anyMatch(action -> Boolean.TRUE.equals(action.blocker())));
        assertTrue(playbook.actions().stream().anyMatch(action -> "CRITICAL".equals(action.severity())));
    }

    @Test
    void playbookActionFeedbackTracksBeforeAfterDeltas() {
        OrderTradePositionPipelineService service = new OrderTradePositionPipelineService();

        Order order = service.createOrder(1L, "QQQ", OrderSide.BUY, new BigDecimal("2"));
        var before = service.captureProfitPlaybookSnapshot(1L);

        service.cancelOrder(order.orderId(), "stale cleanup", "risk@quant.io");

        var after = service.captureProfitPlaybookSnapshot(1L);
        service.appendProfitPlaybookActionFeedback(
                1L,
                "remediateStaleOrders",
                "지연 주문 정리",
                "staleOrders",
                "SUCCESS",
                "test feedback",
                "risk@quant.io",
                before,
                after
        );

        var feedbackRows = service.searchProfitPlaybookActionFeedbacks(1L, 10);
        assertEquals(1, feedbackRows.size());

        var feedback = feedbackRows.get(0);
        assertEquals("remediateStaleOrders", feedback.actionKey());
        assertEquals("staleOrders", feedback.sourceTaskKey());
        assertEquals("SUCCESS", feedback.outcomeStatus());
        assertEquals(-1, feedback.deltaOpenOrderCount());
        assertTrue(feedback.beforeSnapshot() != null);
        assertTrue(feedback.afterSnapshot() != null);
    }

    @Test
    void staleOrderRemediationCancelsMatchingOpenOrders() {
        OrderTradePositionPipelineService service = new OrderTradePositionPipelineService();

        service.createOrder(1L, "IWM", OrderSide.BUY, new BigDecimal("5"));
        service.createOrder(1L, "IWM", OrderSide.BUY, new BigDecimal("7"));
        service.createOrder(1L, "SPY", OrderSide.BUY, new BigDecimal("3"));

        var result = service.remediateStaleOrders(1L, "IWM", 0, "test remediation", "risk@quant.io");
        assertEquals(2, result.canceledCount());
        assertEquals(2, result.items().size());
        assertTrue(result.items().stream().allMatch(item -> "CANCELED".equals(item.currentStatus())));

        var canceledIwms = service.searchOrders(1L, "IWM", "CANCELED");
        assertEquals(2, canceledIwms.size());

        var openSpy = service.searchOrders(1L, "SPY", "NEW");
        assertEquals(1, openSpy.size());
    }

    @Test
    void orderInsightAggregatesExecutionAndAuditSignals() {
        OrderTradePositionPipelineService service = new OrderTradePositionPipelineService();

        Order order = service.createOrder(
                1L,
                "AMZN",
                OrderSide.BUY,
                new BigDecimal("4"),
                OrderType.LIMIT,
                TimeInForce.DAY,
                new BigDecimal("180.00"),
                "admin@quant.io"
        );
        service.applyTrade(order.orderId(), new BigDecimal("2"), new BigDecimal("179.50"), "trader@quant.io");

        var insight = service.getOrderInsight(order.orderId(), 0);
        assertEquals(order.orderId(), insight.order().orderId());
        assertEquals(1, insight.trades().size());
        assertTrue(insight.audits().size() >= 2);
        assertEquals(0, insight.executedNotional().compareTo(new BigDecimal("359.000000")));
        assertTrue(insight.fillRatePct().compareTo(BigDecimal.ZERO) > 0);
    }
}
