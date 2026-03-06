package com.example.samplebatchquartzdashboard.batchsample;

import jakarta.validation.Valid;
import org.springframework.batch.core.JobExecution;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/batch-sample")
public class BatchSampleController {

    private final BatchSampleDatasetService datasetService;
    private final BatchSampleJobService jobService;
    private final BatchSampleMetricsRepository metricsRepository;

    public BatchSampleController(
            BatchSampleDatasetService datasetService,
            BatchSampleJobService jobService,
            BatchSampleMetricsRepository metricsRepository
    ) {
        this.datasetService = datasetService;
        this.jobService = jobService;
        this.metricsRepository = metricsRepository;
    }

    @PostMapping("/seed")
    @ResponseStatus(HttpStatus.CREATED)
    public BatchSampleSeedResponse seed(@Valid @RequestBody BatchSampleSeedRequest request) {
        return datasetService.seed(request);
    }

    @PostMapping("/run")
    public BatchSampleRunResponse run() {
        JobExecution execution = jobService.launchNow();
        BatchSampleMetricsResponse metrics = metricsRepository.fetch();
        return new BatchSampleRunResponse(
                execution.getId(),
                execution.getStatus().name(),
                metrics.processedInput(),
                metrics.outputCount()
        );
    }

    @GetMapping("/metrics")
    public BatchSampleMetricsResponse metrics() {
        return metricsRepository.fetch();
    }
}
