package com.example.samplebatchquartzdashboard;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.samplebatchquartzdashboard.batch.TaskImportJobService;
import com.example.samplebatchquartzdashboard.dashboard.DashboardMetricsRepository;
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

    @Test
    void seededTasksAreProcessedByBatchJob() {
        taskDatasetService.seed(new TaskSeedRequest(500, true));

        JobExecution execution = taskImportJobService.launchNow("TEST");

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(dashboardMetricsRepository.fetchOverview().processedRequests()).isEqualTo(500L);
    }
}
