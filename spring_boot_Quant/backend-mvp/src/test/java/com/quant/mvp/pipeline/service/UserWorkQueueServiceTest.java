package com.quant.mvp.pipeline.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.quant.mvp.pipeline.domain.OrderSide;
import com.quant.mvp.pipeline.payload.AccountWorkQueuePayload;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserWorkQueueServiceTest {

    @Autowired
    private AccessControlService accessControlService;

    @Test
    void viewerGetsReadOnlyStaleOrderTask() {
        OrderTradePositionPipelineService pipeline = new OrderTradePositionPipelineService();
        JournalLedgerService journal = new JournalLedgerService(pipeline);
        UserWorkQueueService service = new UserWorkQueueService(accessControlService, pipeline, journal);

        pipeline.createOrder(1L, "AAPL", OrderSide.BUY, new BigDecimal("1"));

        AccountWorkQueuePayload.Res result = service.build(
                "viewer@quant.io",
                new AccountWorkQueuePayload.Req(1L, 0, 10)
        );

        assertNotNull(result.summary());
        var staleTask = result.tasks().stream()
                .filter(item -> "staleOrders".equals(item.taskKey()))
                .findFirst()
                .orElseThrow();
        assertFalse(Boolean.TRUE.equals(staleTask.actionEnabled()));
    }

    @Test
    void adminGetsActionableStaleOrderTask() {
        OrderTradePositionPipelineService pipeline = new OrderTradePositionPipelineService();
        JournalLedgerService journal = new JournalLedgerService(pipeline);
        UserWorkQueueService service = new UserWorkQueueService(accessControlService, pipeline, journal);

        pipeline.createOrder(1L, "MSFT", OrderSide.BUY, new BigDecimal("2"));

        AccountWorkQueuePayload.Res result = service.build(
                "admin@quant.io",
                new AccountWorkQueuePayload.Req(1L, 0, 10)
        );

        var staleTask = result.tasks().stream()
                .filter(item -> "staleOrders".equals(item.taskKey()))
                .findFirst()
                .orElseThrow();
        assertTrue(Boolean.TRUE.equals(staleTask.actionEnabled()));
    }
}
