package com.example.sampleobservabilityapi.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Service;

@Service
public class ObservedWorkloadService {

    private final ObservationRegistry observationRegistry;
    private final Counter processedCounter;
    private final Counter failedCounter;
    private final Timer durationTimer;
    private final AtomicReference<String> lastOutcome = new AtomicReference<>("NONE");

    public ObservedWorkloadService(MeterRegistry meterRegistry, ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
        this.processedCounter = Counter.builder("sample.workload.processed.total").register(meterRegistry);
        this.failedCounter = Counter.builder("sample.workload.failed.total").register(meterRegistry);
        this.durationTimer = Timer.builder("sample.workload.duration").register(meterRegistry);
    }

    public ProcessingResponse process(ProcessingRequest request) {
        Observation observation = Observation.start("sample.workload.process", observationRegistry);
        observation.lowCardinalityKeyValue("units.bucket", bucket(request.units()));

        long startedAt = System.nanoTime();
        try (Observation.Scope scope = observation.openScope()) {
            if (request.simulateFailure()) {
                failedCounter.increment();
                lastOutcome.set("FAILED");
                throw new IllegalStateException("의도적으로 실패를 발생시켰습니다.");
            }

            sleep(request.units() * 10L);
            processedCounter.increment();
            lastOutcome.set("PROCESSED");
            long durationMs = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
            durationTimer.record(Duration.ofMillis(durationMs));
            return new ProcessingResponse(request.workloadId(), "PROCESSED", durationMs);
        } catch (RuntimeException exception) {
            observation.error(exception);
            long durationMs = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
            durationTimer.record(Duration.ofMillis(durationMs));
            throw exception;
        } finally {
            observation.stop();
        }
    }

    public ProcessingResponse slow(long millis) {
        ProcessingResponse response = process(new ProcessingRequest("SLOW-" + millis, (int) Math.max(1, millis / 10L), false));
        sleep(millis);
        return new ProcessingResponse(response.workloadId(), "SLOW_COMPLETED", response.durationMs() + millis);
    }

    public MetricsSummaryResponse summary() {
        return new MetricsSummaryResponse(
                processedCounter.count(),
                failedCounter.count(),
                durationTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS),
                lastOutcome.get()
        );
    }

    private String bucket(int units) {
        if (units <= 10) {
            return "small";
        }
        if (units <= 50) {
            return "medium";
        }
        return "large";
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("처리 중 인터럽트가 발생했습니다.", exception);
        }
    }
}
