package com.quant.mvp.api;

import com.quant.mvp.pipeline.payload.PositionSearchPayload;
import com.quant.mvp.pipeline.domain.PermissionAction;
import com.quant.mvp.pipeline.service.OrderTradePositionPipelineService;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/positions")
public class PositionController {

    private final OrderTradePositionPipelineService pipelineService;
    private final PermissionGuard permissionGuard;

    public PositionController(
            OrderTradePositionPipelineService pipelineService,
            PermissionGuard permissionGuard
    ) {
        this.pipelineService = pipelineService;
        this.permissionGuard = permissionGuard;
    }

    @GetMapping
    public PositionSearchPayload.Res search(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestParam Long portfolioId
    ) {
        permissionGuard.require(userEmail, "positions", PermissionAction.READ);
        return new PositionSearchPayload.Res(
                pipelineService.getPositions(portfolioId)
                        .stream()
                        .map(p -> new PositionSearchPayload.Item(
                                p.portfolioId(),
                                p.symbol(),
                                p.quantity(),
                                p.avgPrice(),
                                p.lastPrice(),
                                p.marketValue(),
                                p.unrealizedPnl(),
                                p.realizedPnl()
                        ))
                        .collect(Collectors.toList())
        );
    }
}
