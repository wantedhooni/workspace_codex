package com.example.commerce.gateway.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetSocketAddress;
import java.security.Principal;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import reactor.test.StepVerifier;

/**
 * Gateway 요청 제한 키 생성 규칙을 검증한다.
 */
class RequestKeyResolverTest {

    private final RequestKeyResolver resolver = new RequestKeyResolver();

    /**
     * 비인증 요청에서 직접 연결된 클라이언트 IP를 반환하는지 검증한다.
     */
    @Test
    void resolvesRemoteAddressForAnonymousRequest() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/users")
                .remoteAddress(new InetSocketAddress("192.0.2.10", 12345))
                .build();

        StepVerifier.create(resolver.resolve(MockServerWebExchange.from(request)))
                .assertNext(key -> assertThat(key).isEqualTo("192.0.2.10"))
                .verifyComplete();
    }

    /**
     * 인증 주체 이름이 비어 있으면 Reactor null 매핑 예외 없이 원격 주소를 반환하는지 검증한다.
     */
    @Test
    void fallsBackToRemoteAddressWhenPrincipalNameIsNull() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/users")
                .remoteAddress(new InetSocketAddress("192.0.2.11", 12345))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request)
                .principal((Principal) () -> null)
                .build();

        StepVerifier.create(resolver.resolve(exchange))
                .assertNext(key -> assertThat(key).isEqualTo("192.0.2.11"))
                .verifyComplete();
    }
}
