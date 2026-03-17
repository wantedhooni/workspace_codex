package com.example.marketsignal.batch;

import com.example.marketsignal.common.BusinessException;
import com.example.marketsignal.config.AppProperties;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/**
 * Spring Batch와 Quartz JDBC 메타데이터를 직접 조회해 운영 상세 화면에 제공한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchJdbcMetadataService {

    private static final int DEFAULT_LIMIT = 10;

    private final JdbcTemplate jdbcTemplate;
    private final AppProperties appProperties;

    /**
     * 특정 배치 작업의 JDBC 메타데이터 상세를 조회한다.
     */
    public BatchJobMetadataResponse getJobMetadata(String jobName, int limit) {
        ManagedBatchJob managedJob = ManagedBatchJob.fromApiName(jobName);
        int normalizedLimit = Math.min(Math.max(limit, 1), 20);

        try {
            List<BatchExecutionDetailResponse> executions = jdbcTemplate.query(
                    """
                    select e.job_execution_id,
                           e.status,
                           e.exit_code,
                           e.exit_message,
                           e.create_time,
                           e.start_time,
                           e.end_time,
                           e.last_updated
                    from batch_job_execution e
                    join batch_job_instance i on i.job_instance_id = e.job_instance_id
                    where i.job_name = ?
                    order by e.job_execution_id desc
                    limit ?
                    """,
                    jobExecutionRowMapper(),
                    managedJob.springBatchJobName(),
                    normalizedLimit
            ).stream()
                    .map(execution -> new BatchExecutionDetailResponse(
                            execution.executionId(),
                            execution.status(),
                            execution.exitCode(),
                            execution.exitDescription(),
                            execution.createdAt(),
                            execution.startedAt(),
                            execution.endedAt(),
                            execution.lastUpdatedAt(),
                            execution.durationSeconds(),
                            loadExecutionParameters(execution.executionId()),
                            loadStepExecutions(execution.executionId())
                    ))
                    .toList();

            return new BatchJobMetadataResponse(
                    managedJob.apiName(),
                    managedJob.title(),
                    managedJob.description(),
                    buildMetrics(executions),
                    loadQuartzTrigger(managedJob),
                    executions
            );
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("JDBC 배치 메타데이터 조회에 실패했습니다. job={}", managedJob.apiName(), exception);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "배치 JDBC 메타데이터를 조회하지 못했습니다.", exception);
        }
    }

    /**
     * JobExecution 목록을 JDBC ResultSet에서 응답 모델로 변환한다.
     */
    private RowMapper<BatchExecutionDetailResponse> jobExecutionRowMapper() {
        return (resultSet, rowNum) -> {
            OffsetDateTime createdAt = toOffsetDateTime(resultSet.getTimestamp("create_time"));
            OffsetDateTime startedAt = toOffsetDateTime(resultSet.getTimestamp("start_time"));
            OffsetDateTime endedAt = toOffsetDateTime(resultSet.getTimestamp("end_time"));

            return new BatchExecutionDetailResponse(
                    resultSet.getLong("job_execution_id"),
                    resultSet.getString("status"),
                    resultSet.getString("exit_code"),
                    resultSet.getString("exit_message"),
                    createdAt,
                    startedAt,
                    endedAt,
                    toOffsetDateTime(resultSet.getTimestamp("last_updated")),
                    startedAt != null && endedAt != null ? Duration.between(startedAt, endedAt).getSeconds() : null,
                    Map.of(),
                    List.of()
            );
        };
    }

    /**
     * 특정 JobExecution의 실행 파라미터를 JDBC에서 조회한다.
     */
    private Map<String, String> loadExecutionParameters(Long executionId) {
        List<Map.Entry<String, String>> rows = jdbcTemplate.query(
                """
                select parameter_name, parameter_value
                from batch_job_execution_params
                where job_execution_id = ?
                order by parameter_name asc
                """,
                (resultSet, rowNum) -> Map.entry(
                        resultSet.getString("parameter_name"),
                        resultSet.getString("parameter_value")
                ),
                executionId
        );

        LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
        rows.forEach(entry -> parameters.put(entry.getKey(), entry.getValue()));
        return parameters;
    }

    /**
     * 특정 JobExecution에 속한 Step 실행 상세를 JDBC에서 조회한다.
     */
    private List<BatchStepExecutionResponse> loadStepExecutions(Long executionId) {
        return jdbcTemplate.query(
                """
                select step_execution_id,
                       step_name,
                       status,
                       exit_code,
                       exit_message,
                       read_count,
                       write_count,
                       commit_count,
                       rollback_count,
                       start_time,
                       end_time,
                       last_updated
                from batch_step_execution
                where job_execution_id = ?
                order by step_execution_id desc
                """,
                this::mapStepExecution,
                executionId
        );
    }

    /**
     * StepExecution JDBC 행을 응답 모델로 변환한다.
     */
    private BatchStepExecutionResponse mapStepExecution(ResultSet resultSet, int rowNum) throws SQLException {
        return new BatchStepExecutionResponse(
                resultSet.getLong("step_execution_id"),
                resultSet.getString("step_name"),
                resultSet.getString("status"),
                resultSet.getString("exit_code"),
                resultSet.getString("exit_message"),
                resultSet.getLong("read_count"),
                resultSet.getLong("write_count"),
                resultSet.getLong("commit_count"),
                resultSet.getLong("rollback_count"),
                toOffsetDateTime(resultSet.getTimestamp("start_time")),
                toOffsetDateTime(resultSet.getTimestamp("end_time")),
                toOffsetDateTime(resultSet.getTimestamp("last_updated"))
        );
    }

    /**
     * JDBC 실행 목록으로 운영 집계 지표를 계산한다.
     */
    private BatchJdbcMetricsResponse buildMetrics(List<BatchExecutionDetailResponse> executions) {
        long completedExecutions = executions.stream()
                .filter(execution -> "COMPLETED".equalsIgnoreCase(execution.status()))
                .count();
        long failedExecutions = executions.stream()
                .filter(execution -> "FAILED".equalsIgnoreCase(execution.status()))
                .count();
        long runningExecutions = executions.stream()
                .filter(execution -> "STARTED".equalsIgnoreCase(execution.status()) || "STARTING".equalsIgnoreCase(execution.status()))
                .count();

        OffsetDateTime lastSuccessfulAt = executions.stream()
                .filter(execution -> "COMPLETED".equalsIgnoreCase(execution.status()))
                .map(BatchExecutionDetailResponse::endedAt)
                .filter(value -> value != null)
                .findFirst()
                .orElse(null);

        OffsetDateTime lastFailedAt = executions.stream()
                .filter(execution -> "FAILED".equalsIgnoreCase(execution.status()))
                .map(BatchExecutionDetailResponse::endedAt)
                .filter(value -> value != null)
                .findFirst()
                .orElse(null);

        return new BatchJdbcMetricsResponse(
                executions.size(),
                completedExecutions,
                failedExecutions,
                runningExecutions,
                lastSuccessfulAt,
                lastFailedAt
        );
    }

    /**
     * 관리 대상 배치에 연결된 Quartz 트리거 메타데이터를 JDBC에서 조회한다.
     */
    private QuartzTriggerMetadataResponse loadQuartzTrigger(ManagedBatchJob managedJob) {
        if (!managedJob.schedulable()) {
            return null;
        }

        List<QuartzTriggerMetadataResponse> rows = jdbcTemplate.query(
                """
                select t.trigger_name,
                       t.trigger_group,
                       t.trigger_state,
                       ct.cron_expression,
                       ct.time_zone_id,
                       t.start_time,
                       t.next_fire_time,
                       t.prev_fire_time,
                       t.end_time
                from qrtz_triggers t
                left join qrtz_cron_triggers ct
                  on t.sched_name = ct.sched_name
                 and t.trigger_name = ct.trigger_name
                 and t.trigger_group = ct.trigger_group
                where t.job_name = ?
                  and t.job_group = ?
                order by t.trigger_name asc
                """,
                (resultSet, rowNum) -> new QuartzTriggerMetadataResponse(
                        resultSet.getString("trigger_name"),
                        resultSet.getString("trigger_group"),
                        resultSet.getString("trigger_state"),
                        resultSet.getString("cron_expression"),
                        resultSet.getString("time_zone_id"),
                        toOffsetDateTime(resultSet.getLong("start_time")),
                        toOffsetDateTime(resultSet.getLong("next_fire_time")),
                        toOffsetDateTime(resultSet.getLong("prev_fire_time")),
                        toOffsetDateTime(resultSet.getLong("end_time"))
                ),
                managedJob.quartzJobKey().getName(),
                managedJob.quartzJobKey().getGroup()
        );

        return rows.isEmpty() ? null : rows.getFirst();
    }

    /**
     * JDBC timestamp 값을 서버 표준 시간대 기준 OffsetDateTime으로 변환한다.
     */
    private OffsetDateTime toOffsetDateTime(Timestamp value) {
        if (value == null) {
            return null;
        }
        return toOffsetDateTime(value.toLocalDateTime());
    }

    /**
     * JDBC LocalDateTime 값을 서버 표준 시간대 기준 OffsetDateTime으로 변환한다.
     */
    private OffsetDateTime toOffsetDateTime(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        ZoneId zoneId = ZoneId.of(appProperties.batch().report().zoneId());
        return value.atZone(zoneId).toOffsetDateTime();
    }

    /**
     * Quartz epoch milli 값을 서버 표준 시간대 기준 OffsetDateTime으로 변환한다.
     */
    private OffsetDateTime toOffsetDateTime(long epochMillis) {
        if (epochMillis <= 0) {
            return null;
        }
        ZoneId zoneId = ZoneId.of(appProperties.batch().report().zoneId());
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), zoneId);
    }
}
