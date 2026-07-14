package com.example.commerce.gateway.api;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Gateway circuit breaker fallback 응답 계약을 검증한다.
 */
class GatewayFallbackControllerTest {

    private final WebTestClient client =
            WebTestClient.bindToController(new GatewayFallbackController()).build();

    /**
     * 회원 서비스 장애 응답이 503과 명시적인 오류 코드를 반환하는지 검증한다.
     */
    @Test
    void returnsServiceUnavailable() {
        client.get()
                .uri("/fallback/users")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.code").isEqualTo("USER_SERVICE_UNAVAILABLE");
    }

    /**
     * 계좌와 콘텐츠 서비스 장애 응답이 각각 명시적인 오류 코드를 반환하는지 검증한다.
     */
    @Test
    void returnsDomainServiceUnavailableCodes() {
        client.get()
                .uri("/fallback/accounts")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.code").isEqualTo("ACCOUNT_SERVICE_UNAVAILABLE");

        client.get()
                .uri("/fallback/contents")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.code").isEqualTo("CONTENTS_SERVICE_UNAVAILABLE");
    }
}
