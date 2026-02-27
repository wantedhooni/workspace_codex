package com.quant.mvp.api;

import com.quant.mvp.pipeline.domain.Trade;
import com.quant.mvp.pipeline.domain.PermissionAction;
import com.quant.mvp.pipeline.payload.ApplyTradePayload;
import com.quant.mvp.pipeline.payload.TradeListPayload;
import com.quant.mvp.pipeline.service.OrderTradePositionPipelineService;
import jakarta.validation.Valid;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final OrderTradePositionPipelineService pipelineService;
    private final PermissionGuard permissionGuard;

    public TradeController(
            OrderTradePositionPipelineService pipelineService,
            PermissionGuard permissionGuard
    ) {
        this.pipelineService = pipelineService;
        this.permissionGuard = permissionGuard;
    }

    @GetMapping
    public TradeListPayload.Res list(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestParam(required = false) Long portfolioId,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String symbol
    ) {
        permissionGuard.require(userEmail, "trades", PermissionAction.READ);
        return new TradeListPayload.Res(
                pipelineService.searchTrades(portfolioId, orderId, symbol).stream()
                        .map(this::toItem)
                        .collect(Collectors.toList())
        );
    }

    @PostMapping("/events")
    public ApplyTradePayload.Res apply(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @Valid @RequestBody ApplyTradePayload.Req req
    ) {
        permissionGuard.require(userEmail, "trades", PermissionAction.CREATE);
        String actor = permissionGuard.resolveUserEmail(userEmail);
        Trade trade = pipelineService.applyTrade(req.orderId(), req.tradeQuantity(), req.tradePrice(), actor);
        return new ApplyTradePayload.Res(
                trade.tradeId(),
                trade.orderId(),
                trade.symbol(),
                trade.side().name(),
                trade.tradeQuantity(),
                trade.tradePrice(),
                trade.notional(),
                trade.fee(),
                trade.slippage(),
                trade.netCashFlow(),
                trade.tradedAt()
        );
    }

    private TradeListPayload.Item toItem(Trade trade) {
        OrderTradePositionPipelineService.TradeExecutionView tradeView =
                pipelineService.requireTradeExecutionView(trade.tradeId());
        return new TradeListPayload.Item(
                trade.tradeId(),
                trade.orderId(),
                tradeView.portfolioId(),
                trade.symbol(),
                trade.side().name(),
                trade.tradeQuantity(),
                trade.tradePrice(),
                trade.notional(),
                trade.fee(),
                trade.slippage(),
                trade.netCashFlow(),
                trade.tradedAt()
        );
    }
}
