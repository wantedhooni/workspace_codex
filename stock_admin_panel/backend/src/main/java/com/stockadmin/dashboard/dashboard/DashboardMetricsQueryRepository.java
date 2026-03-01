package com.stockadmin.dashboard.dashboard;

import java.util.List;

/**
 * JPA/Querydsl 기반 실제 집계가 붙을 자리다.
 * 현재는 엔티티/메트릭 정의가 없어서 서비스에서 임시 응답을 구성한다.
 */
public interface DashboardMetricsQueryRepository {

    List<DashboardBreakdownRow> fetchBreakdownRows();
}
