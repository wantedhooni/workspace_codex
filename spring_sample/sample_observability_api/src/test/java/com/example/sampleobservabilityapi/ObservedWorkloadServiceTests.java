package com.example.sampleobservabilityapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.sampleobservabilityapi.observability.ObservedWorkloadService;
import com.example.sampleobservabilityapi.observability.ProcessingRequest;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.Test;

class ObservedWorkloadServiceTests {

    @Test
    void incrementsProcessedCounterOnSuccess() {
        ObservedWorkloadService service = new ObservedWorkloadService(new SimpleMeterRegistry(), ObservationRegistry.create());

        var response = service.process(new ProcessingRequest("SYNC-1", 5, false));

        assertThat(response.status()).isEqualTo("PROCESSED");
        assertThat(service.summary().processedCount()).isEqualTo(1.0d);
    }

    @Test
    void incrementsFailedCounterOnFailure() {
        ObservedWorkloadService service = new ObservedWorkloadService(new SimpleMeterRegistry(), ObservationRegistry.create());

        assertThatThrownBy(() -> service.process(new ProcessingRequest("SYNC-2", 5, true)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(service.summary().failedCount()).isEqualTo(1.0d);
    }
}
