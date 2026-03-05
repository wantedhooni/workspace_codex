package com.example.samplebatchquartzdashboard.dashboard;

import com.example.samplebatchquartzdashboard.config.QuartzConfig;
import java.time.Instant;
import java.util.List;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DashboardMetricsRepository {

    private final JdbcTemplate jdbcTemplate;
    private final Scheduler scheduler;

    public DashboardMetricsRepository(JdbcTemplate jdbcTemplate, Scheduler scheduler) {
        this.jdbcTemplate = jdbcTemplate;
        this.scheduler = scheduler;
    }

    public DashboardOverviewResponse fetchOverview() {
        long totalRequests = queryLong("select count(*) from task_import_request");
        long pendingRequests = queryLong("select count(*) from task_import_request where processed = false");
        long processedRequests = queryLong("select count(*) from task_import_request where processed = true");
        long auditRows = queryLong("select count(*) from task_import_audit");
        long batchExecutions = queryLong("select count(*) from batch_job_execution");
        String lastBatchStatus = jdbcTemplate.query(
                "select status from batch_job_execution order by job_execution_id desc limit 1",
                rs -> rs.next() ? rs.getString(1) : "NONE"
        );

        try {
            Trigger.TriggerState triggerState = scheduler.getTriggerState(QuartzConfig.triggerKey());
            Trigger trigger = scheduler.getTrigger(QuartzConfig.triggerKey());
            Instant nextFireTime = trigger != null && trigger.getNextFireTime() != null
                    ? trigger.getNextFireTime().toInstant()
                    : null;
            return new DashboardOverviewResponse(
                    totalRequests,
                    pendingRequests,
                    processedRequests,
                    auditRows,
                    batchExecutions,
                    lastBatchStatus,
                    triggerState.name(),
                    nextFireTime
            );
        } catch (Exception exception) {
            throw new IllegalStateException("대시보드 메트릭 조회에 실패했습니다.", exception);
        }
    }

    public List<RecentAuditResponse> fetchRecentAudits(int limit) {
        return jdbcTemplate.query(
                """
                select
                    request_id,
                    external_id,
                    source_system,
                    account_no,
                    instrument_code,
                    market,
                    settlement_currency,
                    notional_amount,
                    priority,
                    risk_bucket,
                    payload_size,
                    processing_latency_ms,
                    created_at
                from task_import_audit
                order by id desc
                limit ?
                """,
                (rs, rowNum) -> new RecentAuditResponse(
                        rs.getLong("request_id"),
                        rs.getString("external_id"),
                        rs.getString("source_system"),
                        rs.getString("account_no"),
                        rs.getString("instrument_code"),
                        rs.getString("market"),
                        rs.getString("settlement_currency"),
                        rs.getBigDecimal("notional_amount"),
                        rs.getInt("priority"),
                        rs.getString("risk_bucket"),
                        rs.getInt("payload_size"),
                        rs.getLong("processing_latency_ms"),
                        rs.getTimestamp("created_at").toInstant()
                ),
                limit
        );
    }

    private long queryLong(String sql) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class);
        return value == null ? 0L : value;
    }
}
