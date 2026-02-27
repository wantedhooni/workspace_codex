package com.quant.mvp.api;

import com.quant.mvp.pipeline.domain.PermissionAction;
import com.quant.mvp.pipeline.payload.RiskAlertPayload;
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
@RequestMapping("/api/risk-alerts")
public class RiskAlertController {

    private final OrderTradePositionPipelineService pipelineService;
    private final PermissionGuard permissionGuard;

    public RiskAlertController(
            OrderTradePositionPipelineService pipelineService,
            PermissionGuard permissionGuard
    ) {
        this.pipelineService = pipelineService;
        this.permissionGuard = permissionGuard;
    }

    @GetMapping
    public RiskAlertPayload.Res list(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestParam(required = false) Long portfolioId,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String workflowStatus,
            @RequestParam(required = false) Boolean acknowledged,
            @RequestParam(required = false) Boolean slaBreached,
            @RequestParam(required = false) Integer minPriorityScore
    ) {
        permissionGuard.require(userEmail, "riskAlerts", PermissionAction.READ);
        String normalizedSeverity = normalizeFilterString(severity);
        String normalizedCode = normalizeFilterString(code);
        String normalizedWorkflowStatus = normalizeFilterString(workflowStatus);
        int normalizedMinPriorityScore = minPriorityScore == null ? 0 : Math.max(0, minPriorityScore);
        return new RiskAlertPayload.Res(
                pipelineService.searchRiskAlerts(portfolioId).stream()
                        .map(this::toItem)
                        .filter(item -> normalizedSeverity == null || item.severity().equalsIgnoreCase(normalizedSeverity))
                        .filter(item -> normalizedCode == null || item.code().equalsIgnoreCase(normalizedCode))
                        .filter(item -> normalizedWorkflowStatus == null
                                || item.workflowStatus().equalsIgnoreCase(normalizedWorkflowStatus))
                        .filter(item -> acknowledged == null || item.acknowledged().equals(acknowledged))
                        .filter(item -> slaBreached == null || item.slaBreached().equals(slaBreached))
                        .filter(item -> item.priorityScore() >= normalizedMinPriorityScore)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/overview")
    public RiskAlertPayload.OverviewRes overview(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestParam(required = false) Long portfolioId
    ) {
        permissionGuard.require(userEmail, "riskAlerts", PermissionAction.READ);
        return new RiskAlertPayload.OverviewRes(
                pipelineService.searchRiskAlertOverviews(portfolioId).stream()
                        .map(item -> new RiskAlertPayload.OverviewItem(
                                item.portfolioId(),
                                item.totalCount(),
                                item.criticalCount(),
                                item.warnCount(),
                                item.infoCount(),
                                item.unacknowledgedCount(),
                                item.openCount(),
                                item.inProgressCount(),
                                item.resolvedCount(),
                                item.slaBreachedCount(),
                                item.oldestOpenAgeMinutes(),
                                item.avgAckMinutes(),
                                item.avgResolveMinutes(),
                                item.generatedAt()
                        ))
                        .collect(Collectors.toList())
        );
    }

    @PostMapping("/ack")
    public RiskAlertPayload.AckRes acknowledge(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @Valid @RequestBody RiskAlertPayload.AckReq req
    ) {
        permissionGuard.require(userEmail, "riskAlerts", PermissionAction.UPDATE);
        String actor = permissionGuard.resolveUserEmail(userEmail);
        pipelineService.acknowledgeRiskAlert(req.portfolioId(), req.alertKey(), req.note(), actor);
        return new RiskAlertPayload.AckRes(toItem(pipelineService.getRiskAlert(req.portfolioId(), req.alertKey())));
    }

    @PostMapping("/unack")
    public RiskAlertPayload.AckRes unacknowledge(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @Valid @RequestBody RiskAlertPayload.UnackReq req
    ) {
        permissionGuard.require(userEmail, "riskAlerts", PermissionAction.UPDATE);
        String actor = permissionGuard.resolveUserEmail(userEmail);
        pipelineService.clearRiskAlertAcknowledgement(req.portfolioId(), req.alertKey(), actor);
        return new RiskAlertPayload.AckRes(toItem(pipelineService.getRiskAlert(req.portfolioId(), req.alertKey())));
    }

    @PostMapping("/workflow")
    public RiskAlertPayload.AckRes updateWorkflow(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @Valid @RequestBody RiskAlertPayload.WorkflowReq req
    ) {
        permissionGuard.require(userEmail, "riskAlerts", PermissionAction.UPDATE);
        String actor = permissionGuard.resolveUserEmail(userEmail);
        pipelineService.updateRiskAlertWorkflow(
                req.portfolioId(),
                req.alertKey(),
                req.workflowStatus(),
                req.note(),
                req.assignee(),
                actor
        );
        return new RiskAlertPayload.AckRes(toItem(pipelineService.getRiskAlert(req.portfolioId(), req.alertKey())));
    }

    private RiskAlertPayload.Item toItem(OrderTradePositionPipelineService.RiskAlertView alert) {
        OrderTradePositionPipelineService.RiskAlertAcknowledgementView ack =
                pipelineService.getRiskAlertAcknowledgement(alert.portfolioId(), alert.alertKey());
        OrderTradePositionPipelineService.RiskAlertOperationalView operational =
                pipelineService.evaluateRiskAlertOperationalState(alert);
        return new RiskAlertPayload.Item(
                alert.alertKey(),
                alert.portfolioId(),
                alert.severity(),
                alert.code(),
                alert.message(),
                alert.metricName(),
                alert.metricValue(),
                alert.thresholdValue(),
                alert.occurredAt(),
                ack.acknowledged(),
                ack.note(),
                ack.acknowledgedBy(),
                ack.acknowledgedAt(),
                ack.workflowStatus(),
                ack.assignee(),
                ack.resolvedBy(),
                ack.resolvedAt(),
                ack.workflowUpdatedBy(),
                ack.workflowUpdatedAt(),
                operational.ageMinutes(),
                operational.slaTargetMinutes(),
                operational.slaBreached(),
                operational.priorityScore()
        );
    }

    private String normalizeFilterString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
