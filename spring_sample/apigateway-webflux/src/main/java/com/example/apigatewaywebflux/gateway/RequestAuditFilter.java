package com.example.apigatewaywebflux.gateway;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.UUID;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestAuditFilter implements GlobalFilter, Ordered {

    private final MeterRegistry meterRegistry;

    public RequestAuditFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-Id");
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-Correlation-Id", correlationId)
                .build();

        meterRegistry.counter(
                "sample.gateway.webflux.requests",
                "path", exchange.getRequest().getPath().value()
        ).increment();

        exchange.getResponse().getHeaders().set("X-Gateway-Processed", "true");
        exchange.getResponse().getHeaders().set("X-Gateway-App", "apigateway-webflux");

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
