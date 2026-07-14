package com.example.commerce.gateway.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * 상관관계 ID 전역 필터의 생성 및 전파 동작을 검증한다.
 */
class CorrelationIdGlobalFilterTest {

    private final CorrelationIdGlobalFilter filter = new CorrelationIdGlobalFilter();

    /**
     * 요청 ID가 없으면 새 ID를 요청과 응답에 동일하게 설정하는지 검증한다.
     */
    @Test
    void createsCorrelationIdWhenHeaderIsMissing() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/users"));
        GatewayFilterChain chain = currentExchange -> {
            String requestId = currentExchange.getRequest().getHeaders()
                    .getFirst(CorrelationIdGlobalFilter.CORRELATION_ID_HEADER);
            String responseId = currentExchange.getResponse().getHeaders()
                    .getFirst(CorrelationIdGlobalFilter.CORRELATION_ID_HEADER);
            assertThat(requestId).isNotBlank();
            assertThat(responseId).isEqualTo(requestId);
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
    }

    /**
     * 형식이 올바른 클라이언트 요청 ID를 변경하지 않는지 검증한다.
     */
    @Test
    void preservesValidCorrelationId() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/users")
                .header(CorrelationIdGlobalFilter.CORRELATION_ID_HEADER, "checkout-123"));

        StepVerifier.create(filter.filter(exchange, currentExchange -> {
            assertThat(currentExchange.getRequest().getHeaders()
                    .getFirst(CorrelationIdGlobalFilter.CORRELATION_ID_HEADER))
                    .isEqualTo("checkout-123");
            return Mono.empty();
        })).verifyComplete();
    }
}

