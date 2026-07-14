package com.example.commerce.gateway.filter;

import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * 서비스 간 요청 추적에 사용할 상관관계 ID를 검증하고 전파하는 전역 필터다.
 */
@Component
public class CorrelationIdGlobalFilter implements GlobalFilter, Ordered {

    /**
     * 서비스 간 상관관계 ID를 전달하는 HTTP 헤더 이름이다.
     */
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final Pattern VALID_CORRELATION_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");

    /**
     * 상관관계 ID 전역 필터를 생성한다.
     */
    public CorrelationIdGlobalFilter() {
    }

    /**
     * 유효한 클라이언트 상관관계 ID를 재사용하고, 없거나 올바르지 않으면 새 ID를 생성한다.
     *
     * @param exchange 현재 서버 교환 객체
     * @param chain 게이트웨이 필터 체인
     * @return 필터 처리 완료 신호
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = resolveCorrelationId(
                exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER));
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> headers.set(CORRELATION_ID_HEADER, correlationId))
                .build();
        ServerWebExchange mutatedExchange = exchange.mutate().request(request).build();
        mutatedExchange.getResponse().getHeaders().set(CORRELATION_ID_HEADER, correlationId);
        return chain.filter(mutatedExchange);
    }

    /**
     * 애플리케이션 필터보다 먼저 상관관계 ID를 설정하도록 우선순위를 반환한다.
     *
     * @return 필터 우선순위
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private String resolveCorrelationId(String candidate) {
        if (candidate != null && VALID_CORRELATION_ID.matcher(candidate).matches()) {
            return candidate;
        }
        return UUID.randomUUID().toString();
    }
}
