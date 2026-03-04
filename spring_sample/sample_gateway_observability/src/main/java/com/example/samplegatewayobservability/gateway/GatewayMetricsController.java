package com.example.samplegatewayobservability.gateway;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class GatewayMetricsController {

    private final MeterRegistry meterRegistry;

    public GatewayMetricsController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @GetMapping("/gateway-metrics")
    public Map<String, Object> summary() {
        double gatewayRequests = meterRegistry.find("sample.gateway.requests.total").counter() == null
                ? 0.0d
                : meterRegistry.find("sample.gateway.requests.total").counter().count();

        return Map.of(
                "gatewayRequests", gatewayRequests,
                "httpServerRequests", meterRegistry.getMeters().size()
        );
    }
}
