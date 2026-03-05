package com.revy.mvpbanking.admin.application;

import com.revy.mvpbanking.admin.presentation.AdminOverviewResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
class AdminOverviewAlertFactory {

    List<AdminOverviewResponse.Alert> build(AdminOverviewMetrics metrics) {
        List<AdminOverviewResponse.Alert> alerts = new ArrayList<>();
        long staleMarketFeeds = metrics.staleFxPairs() + metrics.staleStockQuotes();

        if (metrics.overdueApprovals() > 0) {
            alerts.add(new AdminOverviewResponse.Alert(
                    "HIGH",
                    "승인 SLA 초과",
                    metrics.overdueApprovals() + "건의 승인 요청이 2시간 이상 대기 중입니다.",
                    "/approvals"
            ));
        }
        if (metrics.reviewRequiredCustomers() > 0) {
            alerts.add(new AdminOverviewResponse.Alert(
                    "HIGH",
                    "심사 필요 고객 누적",
                    metrics.reviewRequiredCustomers() + "명의 고객이 추가 심사를 기다리고 있습니다.",
                    "/customers"
            ));
        }
        if (metrics.lockedAccounts() > 0) {
            alerts.add(new AdminOverviewResponse.Alert(
                    "MEDIUM",
                    "잠금 계좌 모니터링 필요",
                    metrics.lockedAccounts() + "개의 계좌가 LOCKED 상태입니다.",
                    "/accounts"
            ));
        }
        if (metrics.pendingFundingRequests() > 0) {
            alerts.add(new AdminOverviewResponse.Alert(
                    "INFO",
                    "입출금 운영 큐 확인",
                    metrics.pendingFundingRequests() + "건의 입출금 요청이 승인 대기 중입니다.",
                    "/funding-requests"
            ));
        }
        if (staleMarketFeeds > 0) {
            alerts.add(new AdminOverviewResponse.Alert(
                    "HIGH",
                    "시세 데이터 점검 필요",
                    staleMarketFeeds + "개의 FX/주식 시세가 기준 freshness를 초과했습니다.",
                    "/fx-rates"
            ));
        }
        if (metrics.partiallyFilledOrders() > 0) {
            alerts.add(new AdminOverviewResponse.Alert(
                    "MEDIUM",
                    "부분 체결 주문 후속 처리 필요",
                    metrics.partiallyFilledOrders() + "건의 주문이 PARTIALLY_FILLED 상태입니다.",
                    "/stock-orders"
            ));
        }
        if (metrics.pendingApprovals() > 0
                || metrics.pendingFundingRequests() > 0
                || metrics.pendingExchanges() > 0
                || metrics.pendingStockOrders() > 0) {
            alerts.add(new AdminOverviewResponse.Alert(
                    "INFO",
                    "운영 큐 볼륨 확인",
                    "승인/입출금/환전/주식 주문 대기 물량은 총 "
                            + metrics.pendingInstructionVolumeKrw().toPlainString()
                            + " KRW 상당입니다.",
                    "/"
            ));
        }

        if (alerts.isEmpty()) {
            alerts.add(new AdminOverviewResponse.Alert(
                    "STABLE",
                    "운영 상태 안정",
                    "현재 주요 운영 큐와 데이터 freshness 기준은 안정 범위입니다.",
                    "/"
            ));
        }

        return alerts;
    }
}
