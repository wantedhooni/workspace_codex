package com.quant.mvp.api;

import com.quant.mvp.pipeline.domain.PermissionAction;
import com.quant.mvp.pipeline.payload.ExecutionQualityPayload;
import com.quant.mvp.pipeline.service.OrderTradePositionPipelineService;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/execution-qualities")
public class ExecutionQualityController {

    private final OrderTradePositionPipelineService pipelineService;
    private final PermissionGuard permissionGuard;

    public ExecutionQualityController(
            OrderTradePositionPipelineService pipelineService,
            PermissionGuard permissionGuard
    ) {
        this.pipelineService = pipelineService;
        this.permissionGuard = permissionGuard;
    }

    @GetMapping
    public ExecutionQualityPayload.Res list(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestParam(required = false) Long portfolioId,
            @RequestParam(required = false) String symbol
    ) {
        permissionGuard.require(userEmail, "executionQualities", PermissionAction.READ);
        return new ExecutionQualityPayload.Res(
                pipelineService.searchExecutionQualities(portfolioId, symbol).stream()
                        .map(this::toItem)
                        .collect(Collectors.toList())
        );
    }

    private ExecutionQualityPayload.Item toItem(OrderTradePositionPipelineService.ExecutionQualityView row) {
        return new ExecutionQualityPayload.Item(
                row.qualityKey(),
                row.portfolioId(),
                row.symbol(),
                row.orderCount(),
                row.filledOrderCount(),
                row.fillRatePct(),
                row.tradeCount(),
                row.executedQuantity(),
                row.executedNotional(),
                row.averageFillPrice(),
                row.averageFeeBps(),
                row.averageSlippageBps(),
                row.netCashFlow(),
                row.qualityGrade(),
                row.qualityNote(),
                row.lastTradedAt()
        );
    }
}
