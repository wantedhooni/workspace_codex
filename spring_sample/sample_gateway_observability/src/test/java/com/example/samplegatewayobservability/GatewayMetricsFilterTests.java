package com.example.samplegatewayobservability;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.samplegatewayobservability.gateway.GatewayMetricsFilter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class GatewayMetricsFilterTests {

    @Test
    void incrementsCounterForGatewayRequests() throws Exception {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        GatewayMetricsFilter filter = new GatewayMetricsFilter(meterRegistry);

        filter.doFilter(
                new MockHttpServletRequest("GET", "/api/trades/TR-100"),
                new MockHttpServletResponse(),
                (request, response) -> {
                }
        );

        assertThat(meterRegistry.get("sample.gateway.requests.total").counter().count()).isEqualTo(1.0d);
    }
}
