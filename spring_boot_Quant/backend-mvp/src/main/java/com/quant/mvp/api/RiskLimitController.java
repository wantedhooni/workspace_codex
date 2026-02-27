package com.quant.mvp.api;

import com.quant.mvp.pipeline.domain.RiskLimit;
import com.quant.mvp.pipeline.domain.PermissionAction;
import com.quant.mvp.pipeline.payload.TradingControlHistoryPayload;
import com.quant.mvp.pipeline.payload.RiskLimitPayload;
import com.quant.mvp.pipeline.payload.TradingControlPayload;
import com.quant.mvp.pipeline.service.OrderTradePositionPipelineService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/risk-limits")
public class RiskLimitController {

    private final OrderTradePositionPipelineService pipelineService;
    private final PermissionGuard permissionGuard;

    public RiskLimitController(
            OrderTradePositionPipelineService pipelineService,
            PermissionGuard permissionGuard
    ) {
        this.pipelineService = pipelineService;
        this.permissionGuard = permissionGuard;
    }

    @GetMapping
    public RiskLimitPayload.Res list(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestParam(required = false) Long portfolioId
    ) {
        permissionGuard.require(userEmail, "riskLimits", PermissionAction.READ);
        List<RiskLimitPayload.Item> items = pipelineService.searchRiskLimits(portfolioId).stream()
                .map(this::toItem)
                .collect(Collectors.toList());
        return new RiskLimitPayload.Res(items);
    }

    @PutMapping
    public RiskLimitPayload.Res upsert(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @Valid @RequestBody RiskLimitPayload.Req req
    ) {
        permissionGuard.require(userEmail, "riskLimits", PermissionAction.UPDATE);
        RiskLimit updated = pipelineService.upsertRiskLimit(
                req.portfolioId(),
                req.maxOrderNotional(),
                req.maxPositionNotionalPerSymbol(),
                req.maxDailyTurnover(),
                req.maxOpenOrdersPerSymbol(),
                req.commissionBps(),
                req.slippageBps()
        );
        return new RiskLimitPayload.Res(List.of(toItem(updated)));
    }

    @GetMapping("/trading-controls")
    public TradingControlPayload.Res listTradingControls(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestParam(required = false) Long portfolioId
    ) {
        permissionGuard.require(userEmail, "riskLimits", PermissionAction.READ);
        List<TradingControlPayload.Item> items = pipelineService.searchTradingControls(portfolioId).stream()
                .map(this::toTradingControlItem)
                .collect(Collectors.toList());
        return new TradingControlPayload.Res(items);
    }

    @PutMapping("/trading-controls")
    public TradingControlPayload.Res upsertTradingControl(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @Valid @RequestBody TradingControlPayload.Req req
    ) {
        permissionGuard.require(userEmail, "riskLimits", PermissionAction.UPDATE);
        String actor = permissionGuard.resolveUserEmail(userEmail);
        RiskLimit updated = pipelineService.updateTradingControl(
                req.portfolioId(),
                req.tradingEnabled(),
                req.reason(),
                actor
        );
        return new TradingControlPayload.Res(List.of(toTradingControlItem(updated)));
    }

    @GetMapping("/trading-controls/history")
    public TradingControlHistoryPayload.Res listTradingControlHistory(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestParam(required = false) Long portfolioId,
            @RequestParam(required = false) Integer limit
    ) {
        permissionGuard.require(userEmail, "riskLimits", PermissionAction.READ);
        List<TradingControlHistoryPayload.Item> items = pipelineService.searchTradingControlHistory(portfolioId, limit).stream()
                .map(this::toTradingControlHistoryItem)
                .collect(Collectors.toList());
        return new TradingControlHistoryPayload.Res(items);
    }

    private RiskLimitPayload.Item toItem(RiskLimit riskLimit) {
        return new RiskLimitPayload.Item(
                riskLimit.portfolioId(),
                riskLimit.maxOrderNotional(),
                riskLimit.maxPositionNotionalPerSymbol(),
                riskLimit.maxDailyTurnover(),
                riskLimit.maxOpenOrdersPerSymbol(),
                riskLimit.commissionBps(),
                riskLimit.slippageBps(),
                riskLimit.tradingEnabled(),
                riskLimit.killSwitchReason(),
                riskLimit.killSwitchUpdatedAt(),
                riskLimit.killSwitchUpdatedBy()
        );
    }

    private TradingControlPayload.Item toTradingControlItem(RiskLimit riskLimit) {
        return new TradingControlPayload.Item(
                riskLimit.portfolioId(),
                riskLimit.tradingEnabled(),
                riskLimit.killSwitchReason(),
                riskLimit.killSwitchUpdatedAt(),
                riskLimit.killSwitchUpdatedBy()
        );
    }

    private TradingControlHistoryPayload.Item toTradingControlHistoryItem(
            OrderTradePositionPipelineService.TradingControlHistoryView view
    ) {
        return new TradingControlHistoryPayload.Item(
                view.historyId(),
                view.portfolioId(),
                view.previousTradingEnabled(),
                view.tradingEnabled(),
                view.action(),
                view.reason(),
                view.updatedAt(),
                view.updatedBy()
        );
    }
}
