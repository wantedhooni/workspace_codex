package com.quant.mvp.pipeline.service;

import com.quant.mvp.pipeline.domain.OrderStatus;
import com.quant.mvp.pipeline.domain.PermissionAction;
import com.quant.mvp.pipeline.domain.VoucherStatus;
import com.quant.mvp.pipeline.payload.AccountWorkQueuePayload;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class UserWorkQueueService {

    private static final Long DEFAULT_PORTFOLIO_ID = 1L;
    private static final Integer DEFAULT_STALE_MINUTES = 30;
    private static final Integer DEFAULT_TOP_N = 6;

    private final AccessControlService accessControlService;
    private final OrderTradePositionPipelineService pipelineService;
    private final JournalLedgerService journalLedgerService;

    public UserWorkQueueService(
            AccessControlService accessControlService,
            OrderTradePositionPipelineService pipelineService,
            JournalLedgerService journalLedgerService
    ) {
        this.accessControlService = accessControlService;
        this.pipelineService = pipelineService;
        this.journalLedgerService = journalLedgerService;
    }

    public AccountWorkQueuePayload.Res build(String userEmail, AccountWorkQueuePayload.Req req) {
        Long portfolioId = normalizePortfolioId(req.portfolioId());
        int staleMinutes = normalizeStaleMinutes(req.staleMinutes());
        int topN = normalizeTopN(req.topN());

        int openOrderCount = (int) pipelineService.searchOrders(portfolioId, null, null).stream()
                .filter(order -> isOpenOrderStatus(order.status()))
                .count();

        int staleOrderCount = pipelineService.searchOrderHealth(portfolioId, null, staleMinutes).stream()
                .mapToInt(row -> row.staleOrderCount() == null ? 0 : row.staleOrderCount())
                .sum();

        List<OrderTradePositionPipelineService.RiskAlertView> riskAlerts = pipelineService.searchRiskAlerts(portfolioId);
        boolean tradingEnabled = pipelineService.searchTradingControls(portfolioId).stream()
                .findFirst()
                .map(control -> Boolean.TRUE.equals(control.tradingEnabled()))
                .orElse(true);

        int criticalRiskCount = (int) riskAlerts.stream()
                .filter(alert -> "CRITICAL".equalsIgnoreCase(alert.severity()))
                .filter(alert -> !"TRADING_HALTED".equalsIgnoreCase(alert.code()))
                .count();
        int warningRiskCount = (int) riskAlerts.stream()
                .filter(alert -> "WARN".equalsIgnoreCase(alert.severity()))
                .count();
        int tradingHaltedCount = (int) riskAlerts.stream()
                .filter(alert -> "TRADING_HALTED".equalsIgnoreCase(alert.code()))
                .count();

        int approvedVoucherCount = (int) journalLedgerService.searchVouchers(portfolioId, null).stream()
                .filter(voucher -> voucher.status() == VoucherStatus.APPROVED)
                .count();
        int draftVoucherCount = (int) journalLedgerService.searchVouchers(portfolioId, null).stream()
                .filter(voucher -> voucher.status() == VoucherStatus.DRAFT)
                .count();

        int activeSessionCount = (int) accessControlService.listCurrentSessions(userEmail).stream()
                .filter(session -> Boolean.TRUE.equals(session.active()))
                .count();

        boolean canReadOrders = accessControlService.hasMenuPermission(userEmail, "orders", PermissionAction.READ);
        boolean canReadOrderHealth = accessControlService.hasMenuPermission(userEmail, "orderHealth", PermissionAction.READ);
        boolean canUpdateOrderHealth = accessControlService.hasMenuPermission(userEmail, "orderHealth", PermissionAction.UPDATE);
        boolean canReadRiskAlerts = accessControlService.hasMenuPermission(userEmail, "riskAlerts", PermissionAction.READ);
        boolean canUpdateRiskLimits = accessControlService.hasMenuPermission(userEmail, "riskLimits", PermissionAction.UPDATE);
        boolean canReadVouchers = accessControlService.hasMenuPermission(userEmail, "journalVouchers", PermissionAction.READ);
        boolean canUpdateVouchers = accessControlService.hasMenuPermission(userEmail, "journalVouchers", PermissionAction.UPDATE);
        boolean canReadSessions = accessControlService.hasMenuPermission(userEmail, "accountSessions", PermissionAction.READ);
        boolean canUpdateSessions = accessControlService.hasMenuPermission(userEmail, "accountSessions", PermissionAction.UPDATE);

        List<AccountWorkQueuePayload.AlertItem> alerts = new ArrayList<>();
        if (criticalRiskCount > 0 && canReadRiskAlerts) {
            alerts.add(new AccountWorkQueuePayload.AlertItem(
                    "CRITICAL",
                    "치명 리스크 경보 " + criticalRiskCount + "건이 발생했습니다."
            ));
        }
        if (tradingHaltedCount > 0 && canReadRiskAlerts) {
            alerts.add(new AccountWorkQueuePayload.AlertItem(
                    "WARN",
                    "거래가 중지되어 있습니다. 재개 전 치명 경보를 점검하세요."
            ));
        }
        if (staleOrderCount > 0 && canReadOrderHealth) {
            alerts.add(new AccountWorkQueuePayload.AlertItem(
                    "WARN",
                    staleMinutes + "분 초과 지연 주문이 " + staleOrderCount + "건 존재합니다."
            ));
        }
        if (approvedVoucherCount > 0 && canReadVouchers) {
            alerts.add(new AccountWorkQueuePayload.AlertItem(
                    "WARN",
                    "전기 대기 전표가 " + approvedVoucherCount + "건 있습니다."
            ));
        }
        if (activeSessionCount >= 3 && canReadSessions) {
            alerts.add(new AccountWorkQueuePayload.AlertItem(
                    "INFO",
                    "현재 활성 세션이 " + activeSessionCount + "개입니다. 불필요한 세션을 정리하세요."
            ));
        }
        if (alerts.isEmpty()) {
            alerts.add(new AccountWorkQueuePayload.AlertItem("INFO", "즉시 조치가 필요한 작업이 없습니다."));
        }

        List<AccountWorkQueuePayload.TaskItem> tasks = new ArrayList<>();
        if (canReadRiskAlerts && criticalRiskCount > 0) {
            tasks.add(new AccountWorkQueuePayload.TaskItem(
                    "criticalRiskAlerts",
                    "치명 리스크 경보 확인",
                    "치명 경보 발생 시 긴급대응(거래중지+오픈주문 취소)을 실행하세요.",
                    "CRITICAL",
                    criticalRiskCount,
                    canUpdateRiskLimits && tradingEnabled ? "즉시 중지" : "경보 보기",
                    canUpdateRiskLimits ? "/#/riskLimits" : "/#/riskAlerts",
                    canUpdateRiskLimits && tradingEnabled
            ));
        }
        if (canReadRiskAlerts && tradingHaltedCount > 0) {
            tasks.add(new AccountWorkQueuePayload.TaskItem(
                    "resumeTrading",
                    "거래 재개 검증",
                    "거래 재개 전 치명 경보 해소 여부를 점검하고 필요 시 강제 재개를 수행하세요.",
                    "WARN",
                    1,
                    canUpdateRiskLimits ? "재개 실행" : "상세 보기",
                    "/#/riskLimits",
                    canUpdateRiskLimits
            ));
        }
        if (canReadOrderHealth && staleOrderCount > 0) {
            tasks.add(new AccountWorkQueuePayload.TaskItem(
                    "staleOrders",
                    "지연 주문 정리",
                    staleMinutes + "분 이상 대기한 주문을 점검하고 필요 시 정리합니다.",
                    "WARN",
                    staleOrderCount,
                    canUpdateOrderHealth ? "정리 실행" : "상세 보기",
                    "/#/orderHealth",
                    canUpdateOrderHealth
            ));
        }
        if (canReadVouchers && approvedVoucherCount > 0) {
            tasks.add(new AccountWorkQueuePayload.TaskItem(
                    "approvedVouchers",
                    "승인 전표 전기",
                    "승인은 완료되었으나 미전기인 전표를 전기 처리하세요.",
                    "WARN",
                    approvedVoucherCount,
                    canUpdateVouchers ? "전표 처리" : "상세 보기",
                    "/#/journalVouchers",
                    canUpdateVouchers
            ));
        }
        if (canReadVouchers && draftVoucherCount > 0) {
            tasks.add(new AccountWorkQueuePayload.TaskItem(
                    "draftVouchers",
                    "초안 전표 승인",
                    "초안 상태 전표를 검토하고 승인 대기열을 줄이세요.",
                    "INFO",
                    draftVoucherCount,
                    canUpdateVouchers ? "승인 처리" : "상세 보기",
                    "/#/journalVouchers",
                    canUpdateVouchers
            ));
        }
        if (canReadOrders && openOrderCount > 0) {
            tasks.add(new AccountWorkQueuePayload.TaskItem(
                    "openOrders",
                    "오픈 주문 모니터링",
                    "미체결/부분체결 주문의 상태와 체결 진행률을 점검하세요.",
                    "INFO",
                    openOrderCount,
                    "주문 보기",
                    "/#/orders",
                    true
            ));
        }
        if (canReadSessions && activeSessionCount >= 3) {
            tasks.add(new AccountWorkQueuePayload.TaskItem(
                    "activeSessions",
                    "활성 세션 정리",
                    "보안 강화를 위해 불필요한 로그인 세션을 해지하세요.",
                    "INFO",
                    activeSessionCount,
                    canUpdateSessions ? "세션 정리" : "상세 보기",
                    "/#/accountSessions",
                    canUpdateSessions
            ));
        }

        List<AccountWorkQueuePayload.TaskItem> sortedTasks = tasks.stream()
                .sorted(Comparator.comparing((AccountWorkQueuePayload.TaskItem item) -> severityRank(item.severity()))
                        .thenComparing(AccountWorkQueuePayload.TaskItem::count, Comparator.reverseOrder())
                        .thenComparing(AccountWorkQueuePayload.TaskItem::taskKey))
                .limit(topN)
                .toList();

        AccountWorkQueuePayload.Summary summary = new AccountWorkQueuePayload.Summary(
                portfolioId,
                openOrderCount,
                staleOrderCount,
                criticalRiskCount,
                warningRiskCount,
                approvedVoucherCount,
                draftVoucherCount,
                activeSessionCount
        );

        return new AccountWorkQueuePayload.Res(
                new AccountWorkQueuePayload.Req(portfolioId, staleMinutes, topN),
                summary,
                alerts,
                sortedTasks,
                Instant.now()
        );
    }

    private Long normalizePortfolioId(Long portfolioId) {
        if (portfolioId == null || portfolioId <= 0) {
            return DEFAULT_PORTFOLIO_ID;
        }
        return portfolioId;
    }

    private int normalizeStaleMinutes(Integer staleMinutes) {
        if (staleMinutes == null) {
            return DEFAULT_STALE_MINUTES;
        }
        return Math.max(0, staleMinutes);
    }

    private int normalizeTopN(Integer topN) {
        if (topN == null) {
            return DEFAULT_TOP_N;
        }
        return Math.max(1, Math.min(topN, 20));
    }

    private boolean isOpenOrderStatus(OrderStatus status) {
        return status == OrderStatus.NEW || status == OrderStatus.SENT || status == OrderStatus.PARTIAL;
    }

    private int severityRank(String severity) {
        if (Objects.equals(severity, "CRITICAL")) {
            return 0;
        }
        if (Objects.equals(severity, "WARN")) {
            return 1;
        }
        return 2;
    }
}
