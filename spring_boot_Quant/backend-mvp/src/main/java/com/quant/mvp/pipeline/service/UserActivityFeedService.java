package com.quant.mvp.pipeline.service;

import com.quant.mvp.pipeline.domain.OrderStatus;
import com.quant.mvp.pipeline.domain.PermissionAction;
import com.quant.mvp.pipeline.domain.VoucherStatus;
import com.quant.mvp.pipeline.payload.AccountActivityPayload;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class UserActivityFeedService {

    private static final Long DEFAULT_PORTFOLIO_ID = 1L;
    private static final Integer DEFAULT_LIMIT = 20;

    private final AccessControlService accessControlService;
    private final OrderTradePositionPipelineService pipelineService;
    private final JournalLedgerService journalLedgerService;

    public UserActivityFeedService(
            AccessControlService accessControlService,
            OrderTradePositionPipelineService pipelineService,
            JournalLedgerService journalLedgerService
    ) {
        this.accessControlService = accessControlService;
        this.pipelineService = pipelineService;
        this.journalLedgerService = journalLedgerService;
    }

    public AccountActivityPayload.Res build(String userEmail, AccountActivityPayload.Req req) {
        Long portfolioId = normalizePortfolioId(req.portfolioId());
        int limit = normalizeLimit(req.limit());

        boolean canReadOrders = accessControlService.hasMenuPermission(userEmail, "orders", PermissionAction.READ);
        boolean canReadOrderAudits = accessControlService.hasMenuPermission(userEmail, "orderAudits", PermissionAction.READ);
        boolean canReadRiskAlerts = accessControlService.hasMenuPermission(userEmail, "riskAlerts", PermissionAction.READ);
        boolean canReadVouchers = accessControlService.hasMenuPermission(userEmail, "journalVouchers", PermissionAction.READ);

        List<AccountActivityPayload.Item> items = new ArrayList<>();

        if (canReadOrders || canReadOrderAudits) {
            pipelineService.searchOrderAudits(null, portfolioId, null, null, null).stream()
                    .sorted(Comparator.comparing(com.quant.mvp.pipeline.domain.OrderAuditLog::actedAt, Comparator.reverseOrder())
                            .thenComparing(com.quant.mvp.pipeline.domain.OrderAuditLog::auditId, Comparator.reverseOrder()))
                    .limit(limit)
                    .forEach(audit -> items.add(new AccountActivityPayload.Item(
                            "AUDIT_" + audit.auditId(),
                            "ORDER",
                            auditSeverity(audit.action(), audit.toStatus()),
                            "[주문] " + audit.action() + " · " + audit.symbol(),
                            buildAuditDescription(audit),
                            canReadOrders ? "/#/orders" : "/#/orderAudits",
                            audit.actor(),
                            audit.actedAt()
                    )));
        }

        if (canReadRiskAlerts) {
            pipelineService.searchRiskAlerts(portfolioId).stream()
                    .limit(limit)
                    .forEach(alert -> items.add(new AccountActivityPayload.Item(
                            "RISK_" + alert.alertKey(),
                            "RISK",
                            alert.severity(),
                            "[리스크] " + alert.code(),
                            alert.message(),
                            "/#/riskAlerts",
                            null,
                            alert.occurredAt()
                    )));
        }

        if (canReadVouchers) {
            journalLedgerService.searchVouchers(portfolioId, null).stream()
                    .sorted(Comparator.comparing(voucher -> voucher.createdAt(), Comparator.reverseOrder()))
                    .limit(limit)
                    .forEach(voucher -> items.add(new AccountActivityPayload.Item(
                            "VOUCHER_" + voucher.voucherId(),
                            "BOOK",
                            voucherSeverity(voucher.status()),
                            "[전표] " + voucher.voucherNo() + " · " + voucher.status(),
                            voucherDescription(voucher.status(), voucher.tradeId()),
                            "/#/journalVouchers",
                            null,
                            latestVoucherTime(voucher)
                    )));
        }

        List<AccountActivityPayload.Item> merged = items.stream()
                .filter(item -> item.occurredAt() != null)
                .sorted(Comparator.comparing(AccountActivityPayload.Item::occurredAt).reversed()
                        .thenComparing(AccountActivityPayload.Item::activityKey))
                .limit(limit)
                .toList();

        return new AccountActivityPayload.Res(
                new AccountActivityPayload.Req(portfolioId, limit),
                merged
        );
    }

    private Long normalizePortfolioId(Long portfolioId) {
        if (portfolioId == null || portfolioId <= 0) {
            return DEFAULT_PORTFOLIO_ID;
        }
        return portfolioId;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        return Math.max(1, Math.min(limit, 100));
    }

    private String auditSeverity(String action, OrderStatus toStatus) {
        String normalizedAction = action == null ? "" : action.toUpperCase(Locale.ROOT);
        if ("REJECT".equals(normalizedAction) || toStatus == OrderStatus.REJECTED) {
            return "CRITICAL";
        }
        if ("CANCEL".equals(normalizedAction) || toStatus == OrderStatus.CANCELED) {
            return "WARN";
        }
        if ("TRADE_APPLIED".equals(normalizedAction)) {
            return "INFO";
        }
        return "INFO";
    }

    private String buildAuditDescription(com.quant.mvp.pipeline.domain.OrderAuditLog audit) {
        String statusChange = (audit.fromStatus() == null ? "-" : audit.fromStatus().name())
                + " -> " + (audit.toStatus() == null ? "-" : audit.toStatus().name());
        String reason = audit.reason() == null || audit.reason().isBlank() ? "n/a" : audit.reason();
        return "orderId=" + audit.orderId() + ", status=" + statusChange + ", reason=" + reason;
    }

    private String voucherSeverity(VoucherStatus status) {
        if (status == VoucherStatus.APPROVED) {
            return "WARN";
        }
        if (status == VoucherStatus.DRAFT) {
            return "INFO";
        }
        return "INFO";
    }

    private String voucherDescription(VoucherStatus status, Long tradeId) {
        String tradeText = tradeId == null ? "manual" : "tradeId=" + tradeId;
        return "status=" + status + ", " + tradeText;
    }

    private Instant latestVoucherTime(com.quant.mvp.pipeline.domain.JournalVoucher voucher) {
        if (voucher.postedAt() != null) {
            return voucher.postedAt();
        }
        if (voucher.approvedAt() != null) {
            return voucher.approvedAt();
        }
        return voucher.createdAt();
    }
}
