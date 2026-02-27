package com.quant.mvp.api;

import com.quant.mvp.pipeline.domain.AccountSession;
import com.quant.mvp.pipeline.domain.JournalVoucher;
import com.quant.mvp.pipeline.domain.Order;
import com.quant.mvp.pipeline.domain.OrderStatus;
import com.quant.mvp.pipeline.domain.PermissionAction;
import com.quant.mvp.pipeline.domain.RiskLimit;
import com.quant.mvp.pipeline.domain.VoucherStatus;
import com.quant.mvp.pipeline.payload.AccountMenuPayload;
import com.quant.mvp.pipeline.payload.AccountActivityPayload;
import com.quant.mvp.pipeline.payload.AccountProfilePayload;
import com.quant.mvp.pipeline.payload.AccountSessionPayload;
import com.quant.mvp.pipeline.payload.AccountWorkQueueActionPayload;
import com.quant.mvp.pipeline.payload.AccountWorkQueuePayload;
import com.quant.mvp.pipeline.payload.ChangePasswordPayload;
import com.quant.mvp.pipeline.payload.RevokeSessionPayload;
import com.quant.mvp.pipeline.service.AccessControlService;
import com.quant.mvp.pipeline.service.JournalLedgerService;
import com.quant.mvp.pipeline.service.OrderTradePositionPipelineService;
import com.quant.mvp.pipeline.service.UserActivityFeedService;
import com.quant.mvp.pipeline.service.UserWorkQueueService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private static final Long DEFAULT_FEEDBACK_PORTFOLIO_ID = 1L;

    private final AccessControlService accessControlService;
    private final UserWorkQueueService userWorkQueueService;
    private final UserActivityFeedService userActivityFeedService;
    private final OrderTradePositionPipelineService pipelineService;
    private final JournalLedgerService journalLedgerService;
    private final PermissionGuard permissionGuard;

    public AccountController(
            AccessControlService accessControlService,
            UserWorkQueueService userWorkQueueService,
            UserActivityFeedService userActivityFeedService,
            OrderTradePositionPipelineService pipelineService,
            JournalLedgerService journalLedgerService,
            PermissionGuard permissionGuard
    ) {
        this.accessControlService = accessControlService;
        this.userWorkQueueService = userWorkQueueService;
        this.userActivityFeedService = userActivityFeedService;
        this.pipelineService = pipelineService;
        this.journalLedgerService = journalLedgerService;
        this.permissionGuard = permissionGuard;
    }

    @GetMapping("/me")
    public AccountProfilePayload.Res me(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail
    ) {
        String currentUserEmail = permissionGuard.resolveUserEmail(userEmail);
        permissionGuard.require(currentUserEmail, "accountProfile", PermissionAction.READ);
        AccessControlService.UserView me = accessControlService.getCurrentUser(currentUserEmail);
        return new AccountProfilePayload.Res(
                me.user().userId(),
                me.user().email(),
                me.user().name(),
                me.user().status(),
                me.roleCodes(),
                me.user().lastLoginAt(),
                me.user().updatedAt()
        );
    }

    @GetMapping("/menus")
    public AccountMenuPayload.Res menus(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail
    ) {
        String currentUserEmail = permissionGuard.resolveUserEmail(userEmail);
        permissionGuard.require(currentUserEmail, "accountProfile", PermissionAction.READ);
        return new AccountMenuPayload.Res(
                accessControlService.listCurrentUserMenus(currentUserEmail).stream()
                        .map(view -> new AccountMenuPayload.Item(
                                view.menu().menuId(),
                                view.menu().menuKey(),
                                view.menu().menuLabel(),
                                view.menu().path(),
                                view.menu().icon(),
                                view.menu().sortOrder(),
                                view.canRead(),
                                view.canCreate(),
                                view.canUpdate(),
                                view.canDelete()
                        ))
                .collect(Collectors.toList())
        );
    }

    @GetMapping("/work-queue")
    public AccountWorkQueuePayload.Res workQueue(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestParam(required = false) Long portfolioId,
            @RequestParam(required = false) Integer staleMinutes,
            @RequestParam(required = false) Integer topN
    ) {
        String currentUserEmail = permissionGuard.resolveUserEmail(userEmail);
        permissionGuard.require(currentUserEmail, "accountProfile", PermissionAction.READ);
        AccountWorkQueuePayload.Req req = new AccountWorkQueuePayload.Req(portfolioId, staleMinutes, topN);
        return userWorkQueueService.build(currentUserEmail, req);
    }

    @GetMapping("/activity-feed")
    public AccountActivityPayload.Res activityFeed(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestParam(required = false) Long portfolioId,
            @RequestParam(required = false) Integer limit
    ) {
        String currentUserEmail = permissionGuard.resolveUserEmail(userEmail);
        permissionGuard.require(currentUserEmail, "accountProfile", PermissionAction.READ);
        AccountActivityPayload.Req req = new AccountActivityPayload.Req(portfolioId, limit);
        return userActivityFeedService.build(currentUserEmail, req);
    }

    @PostMapping("/work-queue/actions/remediate-stale-orders")
    public AccountWorkQueueActionPayload.RemediateStaleOrders.Res remediateStaleOrders(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @Valid @RequestBody AccountWorkQueueActionPayload.RemediateStaleOrders.Req req
    ) {
        String currentUserEmail = permissionGuard.resolveUserEmail(userEmail);
        permissionGuard.require(currentUserEmail, "orderHealth", PermissionAction.UPDATE);

        return executeWithPlaybookFeedback(
                req.portfolioId(),
                "remediateStaleOrders",
                "지연 주문 정리",
                "staleOrders",
                req.reason(),
                currentUserEmail,
                () -> {
                    Integer staleMinutes = req.staleMinutes() == null ? 30 : req.staleMinutes();
                    OrderTradePositionPipelineService.StaleOrderRemediationResult result = pipelineService.remediateStaleOrders(
                            req.portfolioId(),
                            null,
                            staleMinutes,
                            req.reason(),
                            currentUserEmail
                    );
                    return new AccountWorkQueueActionPayload.RemediateStaleOrders.Res(
                            result.portfolioId(),
                            result.staleThresholdMinutes(),
                            result.canceledCount(),
                            result.executedAt()
                    );
                }
        );
    }

    @PostMapping("/work-queue/actions/revoke-other-sessions")
    public AccountWorkQueueActionPayload.RevokeOtherSessions.Res revokeOtherSessions(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestBody(required = false) AccountWorkQueueActionPayload.RevokeOtherSessions.Req req
    ) {
        String currentUserEmail = permissionGuard.resolveUserEmail(userEmail);
        permissionGuard.require(currentUserEmail, "accountSessions", PermissionAction.UPDATE);

        Long portfolioId = resolveFeedbackPortfolioId(req == null ? null : req.portfolioId());
        String reason = req == null ? "dashboard session hardening" : req.reason();
        return executeWithPlaybookFeedback(
                portfolioId,
                "revokeOtherSessions",
                "활성 세션 정리",
                "activeSessions",
                reason,
                currentUserEmail,
                () -> {
                    List<AccountSession> revoked = accessControlService.revokeOtherActiveSessions(currentUserEmail);
                    return new AccountWorkQueueActionPayload.RevokeOtherSessions.Res(
                            revoked.size(),
                            revoked.stream().map(AccountSession::sessionId).toList(),
                            Instant.now()
                    );
                }
        );
    }

    @PostMapping("/work-queue/actions/post-approved-vouchers")
    public AccountWorkQueueActionPayload.PostApprovedVouchers.Res postApprovedVouchers(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @Valid @RequestBody AccountWorkQueueActionPayload.PostApprovedVouchers.Req req
    ) {
        String currentUserEmail = permissionGuard.resolveUserEmail(userEmail);
        permissionGuard.require(currentUserEmail, "journalVouchers", PermissionAction.UPDATE);

        return executeWithPlaybookFeedback(
                req.portfolioId(),
                "postApprovedVouchers",
                "승인 전표 전기",
                "approvedVouchers",
                req.reason(),
                currentUserEmail,
                () -> {
                    int limit = normalizeVoucherPostLimit(req.limit());
                    List<JournalVoucher> approved = journalLedgerService
                            .searchVouchers(req.portfolioId(), VoucherStatus.APPROVED.name()).stream()
                            .sorted(Comparator.comparing(JournalVoucher::voucherId))
                            .limit(limit)
                            .toList();

                    List<Long> postedIds = new java.util.ArrayList<>();
                    List<String> failedReasons = new java.util.ArrayList<>();
                    for (JournalVoucher voucher : approved) {
                        try {
                            JournalVoucher posted = journalLedgerService.post(voucher.voucherId());
                            postedIds.add(posted.voucherId());
                        } catch (RuntimeException ex) {
                            failedReasons.add("voucherId=" + voucher.voucherId() + ": " + ex.getMessage());
                        }
                    }

                    return new AccountWorkQueueActionPayload.PostApprovedVouchers.Res(
                            req.portfolioId(),
                            approved.size(),
                            postedIds.size(),
                            postedIds,
                            failedReasons,
                            Instant.now()
                    );
                }
        );
    }

    @PostMapping("/work-queue/actions/approve-draft-vouchers")
    public AccountWorkQueueActionPayload.ApproveDraftVouchers.Res approveDraftVouchers(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @Valid @RequestBody AccountWorkQueueActionPayload.ApproveDraftVouchers.Req req
    ) {
        String currentUserEmail = permissionGuard.resolveUserEmail(userEmail);
        permissionGuard.require(currentUserEmail, "journalVouchers", PermissionAction.UPDATE);

        return executeWithPlaybookFeedback(
                req.portfolioId(),
                "approveDraftVouchers",
                "초안 전표 승인",
                "draftVouchers",
                req.reason(),
                currentUserEmail,
                () -> {
                    int limit = normalizeVoucherPostLimit(req.limit());
                    List<JournalVoucher> draft = journalLedgerService
                            .searchVouchers(req.portfolioId(), VoucherStatus.DRAFT.name()).stream()
                            .sorted(Comparator.comparing(JournalVoucher::voucherId))
                            .limit(limit)
                            .toList();

                    List<Long> approvedIds = new java.util.ArrayList<>();
                    List<String> failedReasons = new java.util.ArrayList<>();
                    for (JournalVoucher voucher : draft) {
                        try {
                            JournalVoucher approved = journalLedgerService.approve(voucher.voucherId());
                            approvedIds.add(approved.voucherId());
                        } catch (RuntimeException ex) {
                            failedReasons.add("voucherId=" + voucher.voucherId() + ": " + ex.getMessage());
                        }
                    }

                    return new AccountWorkQueueActionPayload.ApproveDraftVouchers.Res(
                            req.portfolioId(),
                            draft.size(),
                            approvedIds.size(),
                            approvedIds,
                            failedReasons,
                            Instant.now()
                    );
                }
        );
    }

    @PostMapping("/work-queue/actions/pause-trading")
    public AccountWorkQueueActionPayload.PauseTrading.Res pauseTrading(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @Valid @RequestBody AccountWorkQueueActionPayload.PauseTrading.Req req
    ) {
        String currentUserEmail = permissionGuard.resolveUserEmail(userEmail);
        permissionGuard.require(currentUserEmail, "riskLimits", PermissionAction.UPDATE);

        return executeWithPlaybookFeedback(
                req.portfolioId(),
                "pauseTrading",
                "거래 일시중지",
                "pauseTrading",
                req.reason(),
                currentUserEmail,
                () -> {
                    RiskLimit updated = pipelineService.updateTradingControl(
                            req.portfolioId(),
                            false,
                            req.reason(),
                            currentUserEmail
                    );

                    return new AccountWorkQueueActionPayload.PauseTrading.Res(
                            updated.portfolioId(),
                            updated.tradingEnabled(),
                            updated.killSwitchReason(),
                            updated.killSwitchUpdatedAt(),
                            updated.killSwitchUpdatedBy()
                    );
                }
        );
    }

    @PostMapping("/work-queue/actions/emergency-risk-response")
    public AccountWorkQueueActionPayload.EmergencyRiskResponse.Res emergencyRiskResponse(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @Valid @RequestBody AccountWorkQueueActionPayload.EmergencyRiskResponse.Req req
    ) {
        String currentUserEmail = permissionGuard.resolveUserEmail(userEmail);
        permissionGuard.require(currentUserEmail, "riskLimits", PermissionAction.UPDATE);

        return executeWithPlaybookFeedback(
                req.portfolioId(),
                "emergencyRiskResponse",
                "긴급 리스크 대응",
                "criticalRiskAlerts",
                req.reason(),
                currentUserEmail,
                () -> {
                    String reason = req.reason().trim();
                    RiskLimit updated = pipelineService.updateTradingControl(
                            req.portfolioId(),
                            false,
                            "[EMERGENCY] " + reason,
                            currentUserEmail
                    );

                    boolean cancelOpenOrders = !Boolean.FALSE.equals(req.cancelOpenOrders());
                    List<Long> canceledOrderIds = new java.util.ArrayList<>();
                    if (cancelOpenOrders) {
                        List<Order> openOrders = pipelineService.searchOrders(req.portfolioId(), null, null).stream()
                                .filter(order -> isOpenOrderStatus(order.status()))
                                .toList();

                        for (Order order : openOrders) {
                            try {
                                pipelineService.cancelOrder(
                                        order.orderId(),
                                        "[EMERGENCY] " + reason,
                                        currentUserEmail
                                );
                                canceledOrderIds.add(order.orderId());
                            } catch (RuntimeException ignored) {
                                // ignore one-by-one failures to allow best-effort emergency cancellation
                            }
                        }
                    }

                    return new AccountWorkQueueActionPayload.EmergencyRiskResponse.Res(
                            updated.portfolioId(),
                            updated.tradingEnabled(),
                            updated.killSwitchReason(),
                            canceledOrderIds.size(),
                            canceledOrderIds,
                            Instant.now(),
                            currentUserEmail
                    );
                }
        );
    }

    @PostMapping("/work-queue/actions/resume-trading")
    public AccountWorkQueueActionPayload.ResumeTrading.Res resumeTrading(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @Valid @RequestBody AccountWorkQueueActionPayload.ResumeTrading.Req req
    ) {
        String currentUserEmail = permissionGuard.resolveUserEmail(userEmail);
        permissionGuard.require(currentUserEmail, "riskLimits", PermissionAction.UPDATE);

        return executeWithPlaybookFeedback(
                req.portfolioId(),
                "resumeTrading",
                "거래 재개",
                "resumeTrading",
                req.reason(),
                currentUserEmail,
                () -> {
                    OrderTradePositionPipelineService.TradingResumeResult result = pipelineService.resumeTradingWithGuards(
                            req.portfolioId(),
                            req.reason(),
                            currentUserEmail,
                            req.force()
                    );

                    return new AccountWorkQueueActionPayload.ResumeTrading.Res(
                            result.portfolioId(),
                            result.tradingEnabled(),
                            result.resumed(),
                            result.blockedCriticalCount(),
                            result.blockedCodes(),
                            result.blockedMessages(),
                            result.executedAt(),
                            result.executedBy()
                    );
                }
        );
    }

    @PostMapping("/change-password")
    public ChangePasswordPayload.Res changePassword(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @Valid @RequestBody ChangePasswordPayload.Req req
    ) {
        String currentUserEmail = permissionGuard.resolveUserEmail(userEmail);
        permissionGuard.require(currentUserEmail, "accountProfile", PermissionAction.UPDATE);
        AccessControlService.ChangePasswordResult result =
                accessControlService.changeCurrentPassword(currentUserEmail, req.currentPassword(), req.newPassword());
        return new ChangePasswordPayload.Res(
                result.userId(),
                result.changed(),
                result.changedAt()
        );
    }

    @GetMapping("/sessions")
    public AccountSessionPayload.Res sessions(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail
    ) {
        String currentUserEmail = permissionGuard.resolveUserEmail(userEmail);
        permissionGuard.require(currentUserEmail, "accountSessions", PermissionAction.READ);
        return new AccountSessionPayload.Res(
                accessControlService.listCurrentSessions(currentUserEmail).stream()
                        .map(this::toItem)
                        .collect(Collectors.toList())
        );
    }

    @PostMapping("/sessions/{sessionId}/revoke")
    public RevokeSessionPayload.Res revokeSession(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @PathVariable Long sessionId,
            @RequestBody(required = false) RevokeSessionPayload.Req req
    ) {
        String currentUserEmail = permissionGuard.resolveUserEmail(userEmail);
        permissionGuard.require(currentUserEmail, "accountSessions", PermissionAction.UPDATE);
        AccountSession session = accessControlService.revokeCurrentSession(currentUserEmail, sessionId);
        return new RevokeSessionPayload.Res(
                session.sessionId(),
                session.active(),
                session.lastAccessAt()
        );
    }

    private AccountSessionPayload.Item toItem(AccountSession session) {
        return new AccountSessionPayload.Item(
                session.sessionId(),
                session.ipAddress(),
                session.userAgent(),
                session.active(),
                session.createdAt(),
                session.lastAccessAt()
        );
    }

    private int normalizeVoucherPostLimit(Integer limit) {
        if (limit == null) {
            return 20;
        }
        return Math.max(1, Math.min(limit, 100));
    }

    private Long resolveFeedbackPortfolioId(Long portfolioId) {
        if (portfolioId == null || portfolioId <= 0) {
            return DEFAULT_FEEDBACK_PORTFOLIO_ID;
        }
        return portfolioId;
    }

    private <T> T executeWithPlaybookFeedback(
            Long portfolioId,
            String actionKey,
            String actionTitle,
            String sourceTaskKey,
            String reason,
            String actor,
            Supplier<T> actionExecutor
    ) {
        Long targetPortfolioId = resolveFeedbackPortfolioId(portfolioId);
        OrderTradePositionPipelineService.ProfitPlaybookActionSnapshotView beforeSnapshot =
                pipelineService.captureProfitPlaybookSnapshot(targetPortfolioId);

        try {
            T result = actionExecutor.get();
            OrderTradePositionPipelineService.ProfitPlaybookActionSnapshotView afterSnapshot =
                    pipelineService.captureProfitPlaybookSnapshot(targetPortfolioId);
            pipelineService.appendProfitPlaybookActionFeedback(
                    targetPortfolioId,
                    actionKey,
                    actionTitle,
                    sourceTaskKey,
                    "SUCCESS",
                    reason,
                    actor,
                    beforeSnapshot,
                    afterSnapshot
            );
            return result;
        } catch (RuntimeException ex) {
            OrderTradePositionPipelineService.ProfitPlaybookActionSnapshotView afterSnapshot =
                    safeCapturePlaybookSnapshot(targetPortfolioId, beforeSnapshot);
            String failReason = (reason == null || reason.isBlank() ? "" : reason.trim() + " | ")
                    + "error=" + ex.getMessage();
            pipelineService.appendProfitPlaybookActionFeedback(
                    targetPortfolioId,
                    actionKey,
                    actionTitle,
                    sourceTaskKey,
                    "FAILED",
                    failReason,
                    actor,
                    beforeSnapshot,
                    afterSnapshot
            );
            throw ex;
        }
    }

    private OrderTradePositionPipelineService.ProfitPlaybookActionSnapshotView safeCapturePlaybookSnapshot(
            Long portfolioId,
            OrderTradePositionPipelineService.ProfitPlaybookActionSnapshotView fallback
    ) {
        try {
            return pipelineService.captureProfitPlaybookSnapshot(portfolioId);
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    private boolean isOpenOrderStatus(OrderStatus status) {
        return status == OrderStatus.NEW || status == OrderStatus.SENT || status == OrderStatus.PARTIAL;
    }
}
