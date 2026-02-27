package com.quant.mvp.api;

import com.quant.mvp.pipeline.domain.PortfolioSummary;
import com.quant.mvp.pipeline.payload.PortfolioProfitPlaybookFeedbackPayload;
import com.quant.mvp.pipeline.payload.PortfolioProfitPlaybookPayload;
import com.quant.mvp.pipeline.domain.PermissionAction;
import com.quant.mvp.pipeline.payload.PortfolioSummaryInsightPayload;
import com.quant.mvp.pipeline.payload.PortfolioSummaryPayload;
import com.quant.mvp.pipeline.service.OrderTradePositionPipelineService;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio-summaries")
public class PortfolioSummaryController {

    private final OrderTradePositionPipelineService pipelineService;
    private final PermissionGuard permissionGuard;

    public PortfolioSummaryController(
            OrderTradePositionPipelineService pipelineService,
            PermissionGuard permissionGuard
    ) {
        this.pipelineService = pipelineService;
        this.permissionGuard = permissionGuard;
    }

    @GetMapping
    public PortfolioSummaryPayload.Res list(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestParam(required = false) Long portfolioId
    ) {
        permissionGuard.require(userEmail, "portfolioSummaries", PermissionAction.READ);
        return new PortfolioSummaryPayload.Res(
                pipelineService.searchPortfolioSummaries(portfolioId).stream()
                        .map(this::toItem)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/insight")
    public PortfolioSummaryInsightPayload.Res insight(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestParam(required = false) Long portfolioId
    ) {
        permissionGuard.require(userEmail, "portfolioSummaries", PermissionAction.READ);
        return new PortfolioSummaryInsightPayload.Res(
                pipelineService.searchPortfolioSummaryInsights(portfolioId).stream()
                        .map(this::toInsightItem)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/profit-playbook")
    public PortfolioProfitPlaybookPayload.Res profitPlaybook(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestParam(required = false) Long portfolioId
    ) {
        permissionGuard.require(userEmail, "portfolioSummaries", PermissionAction.READ);
        return new PortfolioProfitPlaybookPayload.Res(
                pipelineService.searchPortfolioProfitPlaybooks(portfolioId).stream()
                        .map(this::toPlaybookItem)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/profit-playbook/feedback")
    public PortfolioProfitPlaybookFeedbackPayload.ListFeedback.Res profitPlaybookFeedback(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestParam(required = false) Long portfolioId,
            @RequestParam(required = false) Integer limit
    ) {
        permissionGuard.require(userEmail, "portfolioSummaries", PermissionAction.READ);
        PortfolioProfitPlaybookFeedbackPayload.ListFeedback.Req req =
                new PortfolioProfitPlaybookFeedbackPayload.ListFeedback.Req(portfolioId, limit);
        return new PortfolioProfitPlaybookFeedbackPayload.ListFeedback.Res(
                req,
                pipelineService.searchProfitPlaybookActionFeedbacks(portfolioId, limit).stream()
                        .map(this::toPlaybookFeedbackItem)
                        .toList()
        );
    }

    private PortfolioSummaryPayload.Item toItem(PortfolioSummary summary) {
        return new PortfolioSummaryPayload.Item(
                summary.portfolioId(),
                summary.grossExposure(),
                summary.marketValue(),
                summary.realizedPnl(),
                summary.unrealizedPnl(),
                summary.totalPnl(),
                summary.dailyTurnover(),
                summary.turnoverUsagePct(),
                summary.openOrderCount(),
                summary.filledOrderCount(),
                summary.tradeCount(),
                summary.positionCount()
        );
    }

    private PortfolioSummaryInsightPayload.Item toInsightItem(OrderTradePositionPipelineService.PortfolioSummaryInsightView insight) {
        return new PortfolioSummaryInsightPayload.Item(
                insight.portfolioId(),
                insight.healthScore(),
                insight.healthStatus(),
                insight.pnlMarginPct(),
                insight.turnoverUsagePct(),
                insight.openOrderRatioPct(),
                insight.orderPressurePct(),
                insight.criticalAlertCount(),
                insight.warnAlertCount(),
                insight.tradingEnabled(),
                insight.topConcentrationSymbol(),
                insight.topConcentrationWeightPct(),
                insight.grossExposure(),
                insight.marketValue(),
                insight.realizedPnl(),
                insight.unrealizedPnl(),
                insight.totalPnl(),
                insight.dailyTurnover(),
                insight.openOrderCount(),
                insight.filledOrderCount(),
                insight.tradeCount(),
                insight.positionCount(),
                insight.generatedAt(),
                insight.topExposures().stream()
                        .map(item -> new PortfolioSummaryInsightPayload.TopExposure(
                                item.symbol(),
                                item.quantity(),
                                item.marketValue(),
                                item.grossExposureWeightPct(),
                                item.totalPnl()
                        ))
                        .toList()
        );
    }

    private PortfolioProfitPlaybookPayload.Item toPlaybookItem(
            OrderTradePositionPipelineService.PortfolioProfitPlaybookView playbook
    ) {
        return new PortfolioProfitPlaybookPayload.Item(
                playbook.portfolioId(),
                playbook.objective(),
                playbook.objectiveDetail(),
                playbook.strategyFocus(),
                playbook.marketRegime(),
                playbook.executionGuideline(),
                playbook.tradable(),
                playbook.priorityScore(),
                playbook.blockerCount(),
                playbook.actions().stream()
                        .map(action -> new PortfolioProfitPlaybookPayload.Action(
                                action.actionKey(),
                                action.severity(),
                                action.blocker(),
                                action.title(),
                                action.description(),
                                action.expectedImpact(),
                                action.ownerRole(),
                                action.horizon(),
                                action.path()
                        ))
                        .toList(),
                playbook.generatedAt()
        );
    }

    private PortfolioProfitPlaybookFeedbackPayload.ListFeedback.Item toPlaybookFeedbackItem(
            OrderTradePositionPipelineService.ProfitPlaybookActionFeedbackView feedback
    ) {
        return new PortfolioProfitPlaybookFeedbackPayload.ListFeedback.Item(
                feedback.feedbackId(),
                feedback.portfolioId(),
                feedback.actionKey(),
                feedback.actionTitle(),
                feedback.sourceTaskKey(),
                feedback.outcomeStatus(),
                feedback.reason(),
                feedback.executedBy(),
                feedback.executedAt(),
                toPlaybookFeedbackSnapshot(feedback.beforeSnapshot()),
                toPlaybookFeedbackSnapshot(feedback.afterSnapshot()),
                feedback.deltaTotalPnl(),
                feedback.deltaTurnoverUsagePct(),
                feedback.deltaOpenOrderCount(),
                feedback.deltaCriticalAlertCount(),
                feedback.deltaWarnAlertCount(),
                feedback.outcomeEvaluation()
        );
    }

    private PortfolioProfitPlaybookFeedbackPayload.ListFeedback.Snapshot toPlaybookFeedbackSnapshot(
            OrderTradePositionPipelineService.ProfitPlaybookActionSnapshotView snapshot
    ) {
        return new PortfolioProfitPlaybookFeedbackPayload.ListFeedback.Snapshot(
                snapshot.portfolioId(),
                snapshot.healthScore(),
                snapshot.healthStatus(),
                snapshot.totalPnl(),
                snapshot.turnoverUsagePct(),
                snapshot.openOrderCount(),
                snapshot.criticalAlertCount(),
                snapshot.warnAlertCount(),
                snapshot.tradingEnabled(),
                snapshot.capturedAt()
        );
    }
}
