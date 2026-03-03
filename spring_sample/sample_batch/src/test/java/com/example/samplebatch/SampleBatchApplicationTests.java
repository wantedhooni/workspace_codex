package com.example.samplebatch;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.samplebatch.batch.TradeSettlementJobService;
import com.example.samplebatch.trade.TradeDatasetService;
import com.example.samplebatch.trade.TradeMetricsRepository;
import com.example.samplebatch.trade.TradeSeedRequest;
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
class SampleBatchApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("sample_batch")
            .withUsername("sample_batch")
            .withPassword("sample_batch");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.trade-settlement.cron", () -> "0 0 0 1 1 ? 2099");
    }

    @Autowired
    private TradeDatasetService tradeDatasetService;

    @Autowired
    private TradeSettlementJobService tradeSettlementJobService;

    @Autowired
    private TradeMetricsRepository tradeMetricsRepository;

    @Test
    void seededTradesAreProcessedByBatchJob() {
        tradeDatasetService.seed(new TradeSeedRequest(1_000, 200, true));

        JobExecution execution = tradeSettlementJobService.launchNow("TEST");

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        var metrics = tradeMetricsRepository.fetchMetrics();
        assertThat(metrics.rawTotal()).isEqualTo(1_000L);
        assertThat(metrics.rawPending()).isZero();
        assertThat(metrics.rawProcessed()).isEqualTo(1_000L);
        assertThat(metrics.summaryTotal()).isEqualTo(1_000L);
    }
}
