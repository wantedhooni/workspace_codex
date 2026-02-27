package com.quant.mvp.pipeline.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.quant.mvp.pipeline.domain.OrderSide;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GlobalSearchServiceTest {

    @Autowired
    private AccessControlService accessControlService;

    @Test
    void viewerSearchDoesNotExposeOrdersSection() {
        OrderTradePositionPipelineService pipelineService = new OrderTradePositionPipelineService();
        PortfolioCatalogService portfolioCatalogService = new PortfolioCatalogService();
        SavedViewService savedViewService = new SavedViewService();

        pipelineService.createOrder(1L, "AAPL", OrderSide.BUY, new BigDecimal("2"));

        GlobalSearchService service = new GlobalSearchService(
                pipelineService,
                accessControlService,
                portfolioCatalogService,
                savedViewService
        );

        var snapshot = service.search("viewer@quant.io", null, 20);

        assertFalse(snapshot.sections().stream().anyMatch(section -> "orders".equals(section.resourceKey())));
        assertTrue(snapshot.sections().stream().anyMatch(section -> "navigation".equals(section.resourceKey())));
    }

    @Test
    void adminSearchIncludesOrdersAndUsers() {
        OrderTradePositionPipelineService pipelineService = new OrderTradePositionPipelineService();
        PortfolioCatalogService portfolioCatalogService = new PortfolioCatalogService();
        SavedViewService savedViewService = new SavedViewService();

        pipelineService.createOrder(1L, "MSFT", OrderSide.BUY, new BigDecimal("3"));

        GlobalSearchService service = new GlobalSearchService(
                pipelineService,
                accessControlService,
                portfolioCatalogService,
                savedViewService
        );

        var snapshot = service.search("admin@quant.io", null, 20);

        assertTrue(snapshot.sections().stream().anyMatch(section -> "orders".equals(section.resourceKey())));
        assertTrue(snapshot.sections().stream().anyMatch(section -> "users".equals(section.resourceKey())));
    }
}
