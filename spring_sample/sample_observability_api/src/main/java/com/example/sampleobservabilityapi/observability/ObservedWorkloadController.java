package com.example.sampleobservabilityapi.observability;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ObservedWorkloadController {

    private final ObservedWorkloadService observedWorkloadService;

    public ObservedWorkloadController(ObservedWorkloadService observedWorkloadService) {
        this.observedWorkloadService = observedWorkloadService;
    }

    @PostMapping("/workloads/process")
    public ProcessingResponse process(@Valid @RequestBody ProcessingRequest request) {
        return observedWorkloadService.process(request);
    }

    @GetMapping("/workloads/slow/{millis}")
    public ProcessingResponse slow(@PathVariable long millis) {
        return observedWorkloadService.slow(millis);
    }

    @GetMapping("/admin/metrics-summary")
    public MetricsSummaryResponse summary() {
        return observedWorkloadService.summary();
    }
}
