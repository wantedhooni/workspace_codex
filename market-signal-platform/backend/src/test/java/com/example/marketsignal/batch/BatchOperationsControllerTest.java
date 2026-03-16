package com.example.marketsignal.batch;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.marketsignal.auth.JwtAuthenticationFilter;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BatchOperationsController.class)
@AutoConfigureMockMvc(addFilters = false)
class BatchOperationsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BatchOperationsService batchOperationsService;

    @MockBean
    private BatchJdbcMetadataService batchJdbcMetadataService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private BatchJobStatusResponse sampleResponse;
    private BatchJobMetadataResponse sampleMetadata;

    @BeforeEach
    void setUp() {
        sampleResponse = sampleResponse();
        sampleMetadata = sampleMetadata();
    }

    @Test
    @DisplayName("배치 작업 목록 API는 현재 스케줄과 최근 실행 이력을 반환한다")
    void getJobsReturnsManagedStatuses() throws Exception {
        when(batchOperationsService.getManagedJobs()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/batch/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jobName").value("daily-report-generate"))
                .andExpect(jsonPath("$[0].scheduleState").value("SCHEDULED"))
                .andExpect(jsonPath("$[0].recentExecutions[0].executionId").value(101L));
    }

    @Test
    @DisplayName("배치 작업 상세 상태 API는 단건 상태를 반환한다")
    void getJobReturnsManagedStatus() throws Exception {
        when(batchOperationsService.getManagedJob("daily-report-generate")).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/batch/jobs/daily-report-generate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobName").value("daily-report-generate"))
                .andExpect(jsonPath("$.lastExecution.executionId").value(101L));
    }

    @Test
    @DisplayName("배치 JDBC 메타데이터 API는 실행 상세와 Quartz 정보를 반환한다")
    void getJobMetadataReturnsJdbcDetails() throws Exception {
        when(batchJdbcMetadataService.getJobMetadata("daily-report-generate", 10)).thenReturn(sampleMetadata);

        mockMvc.perform(get("/api/batch/jobs/daily-report-generate/metadata"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobName").value("daily-report-generate"))
                .andExpect(jsonPath("$.metrics.totalExecutions").value(3))
                .andExpect(jsonPath("$.executions[0].parameters.requestedBy").value("manual"))
                .andExpect(jsonPath("$.executions[0].stepExecutions[0].stepName").value("dailyReportGenerationStep"))
                .andExpect(jsonPath("$.quartzTrigger.triggerName").value("dailyReportQuartzTrigger"));
    }

    @Test
    @DisplayName("배치 수동 실행 API는 실행 후 최신 상태를 반환한다")
    void runJobReturnsUpdatedStatus() throws Exception {
        when(batchOperationsService.runJob("daily-report-generate")).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/batch/jobs/daily-report-generate/run"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobName").value("daily-report-generate"))
                .andExpect(jsonPath("$.lastExecution.status").value("COMPLETED"));
    }

    private BatchJobStatusResponse sampleResponse() {
        BatchExecutionResponse execution = new BatchExecutionResponse(
                101L,
                "COMPLETED",
                "COMPLETED",
                "reportDate=2026-03-16",
                OffsetDateTime.parse("2026-03-16T08:00:00+09:00"),
                OffsetDateTime.parse("2026-03-16T08:00:03+09:00")
        );
        return new BatchJobStatusResponse(
                "daily-report-generate",
                "오늘의 리포트 생성",
                "최신 스냅샷을 기준으로 시장 레짐과 종목 시그널을 계산하고 일간 리포트를 생성합니다.",
                true,
                "SCHEDULED",
                "0 0 8 ? * MON-FRI",
                "Asia/Seoul",
                OffsetDateTime.parse("2026-03-17T08:00:00+09:00"),
                OffsetDateTime.parse("2026-03-16T08:00:00+09:00"),
                false,
                execution,
                List.of(execution)
        );
    }

    private BatchJobMetadataResponse sampleMetadata() {
        BatchStepExecutionResponse stepExecution = new BatchStepExecutionResponse(
                201L,
                "dailyReportGenerationStep",
                "COMPLETED",
                "COMPLETED",
                "reportDate=2026-03-16",
                0L,
                0L,
                1L,
                0L,
                OffsetDateTime.parse("2026-03-16T08:00:00+09:00"),
                OffsetDateTime.parse("2026-03-16T08:00:03+09:00"),
                OffsetDateTime.parse("2026-03-16T08:00:03+09:00")
        );
        BatchExecutionDetailResponse execution = new BatchExecutionDetailResponse(
                101L,
                "COMPLETED",
                "COMPLETED",
                "reportDate=2026-03-16",
                OffsetDateTime.parse("2026-03-16T07:59:59+09:00"),
                OffsetDateTime.parse("2026-03-16T08:00:00+09:00"),
                OffsetDateTime.parse("2026-03-16T08:00:03+09:00"),
                OffsetDateTime.parse("2026-03-16T08:00:03+09:00"),
                3L,
                java.util.Map.of("requestedBy", "manual", "managedJobName", "daily-report-generate"),
                List.of(stepExecution)
        );
        return new BatchJobMetadataResponse(
                "daily-report-generate",
                "오늘의 리포트 생성",
                "최신 스냅샷을 기준으로 시장 레짐과 종목 시그널을 계산하고 일간 리포트를 생성합니다.",
                new BatchJdbcMetricsResponse(
                        3L,
                        2L,
                        1L,
                        0L,
                        OffsetDateTime.parse("2026-03-16T08:00:03+09:00"),
                        OffsetDateTime.parse("2026-03-15T08:00:02+09:00")
                ),
                new QuartzTriggerMetadataResponse(
                        "dailyReportQuartzTrigger",
                        "market-signal",
                        "WAITING",
                        "0 0 8 ? * MON-FRI",
                        "Asia/Seoul",
                        OffsetDateTime.parse("2026-03-16T08:00:00+09:00"),
                        OffsetDateTime.parse("2026-03-17T08:00:00+09:00"),
                        OffsetDateTime.parse("2026-03-16T08:00:00+09:00"),
                        null
                ),
                List.of(execution)
        );
    }
}
