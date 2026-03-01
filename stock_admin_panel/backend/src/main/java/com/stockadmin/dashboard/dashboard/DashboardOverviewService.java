package com.stockadmin.dashboard.dashboard;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardOverviewService {

    public DashboardOverviewResponse getOverview() {
        LocalDate asOfDate = LocalDate.now();

        return new DashboardOverviewResponse(
                "운영 대시보드",
                "주문 처리와 리스크 확인이 동시에 가능한 기본 셸",
                "가정: 종목/계좌/시장 기준 운영 현황을 보는 관리자 화면이며, 실제 엔티티 정의 전까지는 샘플 집계를 반환합니다.",
                asOfDate,
                buildHeadlineMetrics(),
                buildTrendPoints(),
                buildBreakdownRows()
        );
    }

    private List<DashboardMetricCard> buildHeadlineMetrics() {
        return List.of(
                new DashboardMetricCard("aum", "총 운용 잔고", "₩128.4B", "전일 대비 +2.4%", 2.4, "positive"),
                new DashboardMetricCard("orders", "금일 처리 주문", "1,284", "목표 대비 91%", 91.0, "neutral"),
                new DashboardMetricCard("approvals", "대기 승인", "18", "30분 초과 4건", -4.0, "warning"),
                new DashboardMetricCard("alerts", "오픈 알림", "6", "치명도 높음 2건", -2.0, "critical")
        );
    }

    private List<DashboardTrendPoint> buildTrendPoints() {
        return List.of(
                new DashboardTrendPoint("02-22", 142),
                new DashboardTrendPoint("02-23", 156),
                new DashboardTrendPoint("02-24", 161),
                new DashboardTrendPoint("02-25", 149),
                new DashboardTrendPoint("02-26", 173),
                new DashboardTrendPoint("02-27", 181),
                new DashboardTrendPoint("02-28", 176)
        );
    }

    private List<DashboardBreakdownRow> buildBreakdownRows() {
        return List.of(
                new DashboardBreakdownRow("국내 주식", "트레이딩 1팀", "안정", 412, "₩32.6B", 1),
                new DashboardBreakdownRow("해외 주식", "글로벌 데스크", "주의", 358, "₩41.2B", 3),
                new DashboardBreakdownRow("ETF", "패시브 운용", "안정", 271, "₩22.5B", 0),
                new DashboardBreakdownRow("파생/헤지", "리스크 오퍼레이션", "점검", 243, "₩32.1B", 2)
        );
    }
}
