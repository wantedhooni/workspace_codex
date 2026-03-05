package com.revy.mvpbanking.admin.application;

import com.revy.mvpbanking.audit.application.AuditLogService;
import com.revy.mvpbanking.audit.domain.AuditActionType;
import com.revy.mvpbanking.admin.presentation.AdminOverviewResponse;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminOverviewService {

    private final AdminOverviewDataLoader dataLoader;
    private final AdminOverviewMetricsCalculator metricsCalculator;
    private final AdminOverviewAlertFactory alertFactory;
    private final AuditLogService auditLogService;

    public AdminOverviewService(
            AdminOverviewDataLoader dataLoader,
            AdminOverviewMetricsCalculator metricsCalculator,
            AdminOverviewAlertFactory alertFactory,
            AuditLogService auditLogService
    ) {
        this.dataLoader = dataLoader;
        this.metricsCalculator = metricsCalculator;
        this.alertFactory = alertFactory;
        this.auditLogService = auditLogService;
    }

    public AdminOverviewResponse getOverview() {
        auditLogService.logCurrentActor(AuditActionType.ADMIN_OVERVIEW_VIEWED, "ADMIN_OVERVIEW", "dashboard", "Viewed admin overview");

        AdminOverviewDataset dataset = dataLoader.load();
        AdminOverviewMetrics metrics = metricsCalculator.calculate(dataset, Instant.now());

        return new AdminOverviewResponse(
                new AdminOverviewResponse.Metrics(
                        metrics.pendingApprovals(),
                        metrics.overdueApprovals(),
                        metrics.reviewRequiredCustomers(),
                        metrics.lockedAccounts(),
                        metrics.pendingFundingRequests(),
                        metrics.pendingExchanges(),
                        metrics.pendingStockOrders(),
                        metrics.partiallyFilledOrders(),
                        metrics.pendingInstructionVolumeKrw()
                ),
                new AdminOverviewResponse.MarketStatus(
                        metrics.latestFxEffectiveAt(),
                        metrics.latestStockQuoteEffectiveAt(),
                        metrics.freshFxPairs(),
                        metrics.staleFxPairs(),
                        metrics.freshStockQuotes(),
                        metrics.staleStockQuotes()
                ),
                alertFactory.build(metrics)
        );
    }
}
