package com.example.samplegrafanaprometheus.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
public class ObservedWorkloadService {

    private final MeterRegistry meterRegistry;
    private final DistributionSummary volumeSummary;

    public ObservedWorkloadService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.volumeSummary = DistributionSummary.builder("sample_workload_volume")
                .description("처리된 업무 volume")
                .register(meterRegistry);
    }

    public ProcessingResponse process(ProcessingRequest request) {
        long actualDurationMs = Math.max(25L, request.expectedDurationMs() + ThreadLocalRandom.current().nextLong(-20, 40));
        Timer.Sample sample = Timer.start(meterRegistry);
        boolean failed = "FAIL".equalsIgnoreCase(request.channel());
        sleep(actualDurationMs);
        sample.stop(Timer.builder("sample_workload_latency_seconds")
                .tag("channel", request.channel())
                .tag("workType", request.workType())
                .register(meterRegistry));
        volumeSummary.record(request.volume());

        Counter.builder("sample_workload_processed_total")
                .tag("channel", request.channel())
                .register(meterRegistry)
                .increment();

        if (failed) {
            Counter.builder("sample_workload_failed_total")
                    .tag("channel", request.channel())
                    .register(meterRegistry)
                    .increment();
        }

        return new ProcessingResponse(request.channel(), request.workType(), request.volume(), actualDurationMs, failed ? "FAILED" : "PROCESSED");
    }

    public MetricsSummaryResponse summary() {
        double processed = findValue("sample_workload_processed_total");
        double failed = findValue("sample_workload_failed_total");
        double avgLatencyMs = meterRegistry.find("sample_workload_latency_seconds").timer() == null
                ? 0.0
                : meterRegistry.find("sample_workload_latency_seconds").timer().mean(TimeUnit.MILLISECONDS);
        return new MetricsSummaryResponse(processed, failed, avgLatencyMs);
    }

    private double findValue(String name) {
        return meterRegistry.find(name).meters().stream()
                .mapToDouble(meter -> meter.measure().iterator().hasNext() ? meter.measure().iterator().next().getValue() : 0.0)
                .sum();
    }

    private void sleep(long durationMs) {
        try {
            Thread.sleep(durationMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("업무 처리 중 인터럽트 발생", ex);
        }
    }
}
