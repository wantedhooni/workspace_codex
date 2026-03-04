package com.example.samplegatewayobservability.gateway;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class GatewayMetricsFilter extends OncePerRequestFilter {

    private final Counter gatewayRequestCounter;

    public GatewayMetricsFilter(MeterRegistry meterRegistry) {
        this.gatewayRequestCounter = Counter.builder("sample.gateway.requests.total").register(meterRegistry);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/api/")) {
            gatewayRequestCounter.increment();
        }
        filterChain.doFilter(request, response);
    }
}
