package com.example.samplegrafanaprometheus;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.samplegrafanaprometheus.observability.ObservedWorkloadService;
import com.example.samplegrafanaprometheus.observability.ProcessingRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SampleGrafanaPrometheusApplicationTests {

    @Autowired
    private ObservedWorkloadService observedWorkloadService;

    @Test
    void processingUpdatesMetricsSummary() {
        observedWorkloadService.process(new ProcessingRequest("trading-api", "pricing", 100, 50));

        assertThat(observedWorkloadService.summary().processedTotal()).isGreaterThanOrEqualTo(1.0);
    }
}
