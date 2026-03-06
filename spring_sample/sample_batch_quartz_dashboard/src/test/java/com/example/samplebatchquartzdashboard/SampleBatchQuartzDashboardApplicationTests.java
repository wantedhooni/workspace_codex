package com.example.samplebatchquartzdashboard;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.samplebatchquartzdashboard.batchsample.BatchSampleDatasetService;
import com.example.samplebatchquartzdashboard.batchsample.BatchSampleJobService;
import com.example.samplebatchquartzdashboard.batchsample.BatchSampleMetricsRepository;
import com.example.samplebatchquartzdashboard.batchsample.BatchSampleSeedRequest;
import com.example.samplebatchquartzdashboard.batch.TaskImportJobService;
import com.example.samplebatchquartzdashboard.dashboard.DashboardMetricsRepository;
import com.example.samplebatchquartzdashboard.dashboard.QuartzControlService;
import com.example.samplebatchquartzdashboard.dashboard.TaskDatasetService;
import com.example.samplebatchquartzdashboard.dashboard.TaskSeedRequest;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class SampleBatchQuartzDashboardApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("sample_batch_dashboard")
            .withUsername("sample_batch_dashboard")
            .withPassword("sample_batch_dashboard");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.batch-dashboard.cron", () -> "0 0 0 1 1 ? 2099");
    }

    @Autowired
    private TaskDatasetService taskDatasetService;

    @Autowired
    private TaskImportJobService taskImportJobService;

    @Autowired
    private DashboardMetricsRepository dashboardMetricsRepository;

    @Autowired
    private QuartzControlService quartzControlService;

    @Autowired
    private BatchSampleDatasetService batchSampleDatasetService;

    @Autowired
    private BatchSampleJobService batchSampleJobService;

    @Autowired
    private BatchSampleMetricsRepository batchSampleMetricsRepository;

    @Test
    void seededTasksAreProcessedByBatchJob() {
        taskDatasetService.seed(new TaskSeedRequest(500, true));

        JobExecution execution = taskImportJobService.launchNow("TEST");

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(dashboardMetricsRepository.fetchOverview().processedRequests()).isEqualTo(500L);
    }

    @Test
    void realSampleTasksAreProcessedByBatchJob() {
        var seedResponse = taskDatasetService.seedRealSample(true);
        JobExecution execution = taskImportJobService.launchNow("REAL_SAMPLE_TEST");

        assertThat(seedResponse.datasetType()).isEqualTo("REAL_SAMPLE");
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(dashboardMetricsRepository.fetchOverview().processedRequests()).isEqualTo(seedResponse.loadedCount());
        assertThat(dashboardMetricsRepository.fetchRecentAudits(5)).isNotEmpty();
    }

    @Test
    void quartzTriggerCanPauseAndResume() {
        assertThat(quartzControlService.pauseTrigger().triggerState()).isIn("PAUSED", "NORMAL");
        assertThat(quartzControlService.resumeTrigger().triggerState()).isIn("NORMAL", "BLOCKED");
        assertThat(quartzControlService.status().cronExpression()).isNotBlank();
    }

    @Test
    void batchComponentSampleRunsWithReaderProcessorWriterChunk() {
        batchSampleDatasetService.seed(new BatchSampleSeedRequest(1_200, true));

        JobExecution execution = batchSampleJobService.launchNow();

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(batchSampleMetricsRepository.fetch().processedInput()).isEqualTo(1_200L);
        assertThat(batchSampleMetricsRepository.fetch().outputCount()).isEqualTo(1_200L);
    }
}
