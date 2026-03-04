package com.example.samplegrafanaprometheus.observability;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ObservedWorkloadController {

    private final ObservedWorkloadService observedWorkloadService;

    public ObservedWorkloadController(ObservedWorkloadService observedWorkloadService) {
        this.observedWorkloadService = observedWorkloadService;
    }

    @PostMapping("/api/ops/workloads")
    public ProcessingResponse process(@Valid @RequestBody ProcessingRequest request) {
        return observedWorkloadService.process(request);
    }

    @GetMapping("/api/ops/metrics/summary")
    public MetricsSummaryResponse summary() {
        return observedWorkloadService.summary();
    }
}
