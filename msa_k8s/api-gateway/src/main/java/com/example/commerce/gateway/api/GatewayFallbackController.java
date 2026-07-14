package com.example.commerce.gateway.api;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

/**
 * 하위 서비스 circuit breaker가 열렸을 때 일관된 503 응답을 제공한다.
 */
@RestController
public class GatewayFallbackController {

    /**
     * Gateway fallback 컨트롤러를 생성한다.
     */
    public GatewayFallbackController() {
    }

    /**
     * User Service 장애 시 재시도 가능한 서비스 불가 응답을 반환한다.
     *
     * @return 503 오류 응답
     */
    @RequestMapping("/fallback/users")
    public Mono<ResponseEntity<Map<String, Object>>> usersFallback() {
        return unavailable("USER_SERVICE_UNAVAILABLE", "회원 서비스를 일시적으로 사용할 수 없습니다.");
    }

    /**
     * Account Service 장애 시 재시도 가능한 서비스 불가 응답을 반환한다.
     *
     * @return 503 오류 응답
     */
    @RequestMapping("/fallback/accounts")
    public Mono<ResponseEntity<Map<String, Object>>> accountsFallback() {
        return unavailable("ACCOUNT_SERVICE_UNAVAILABLE", "계좌 서비스를 일시적으로 사용할 수 없습니다.");
    }

    /**
     * Contents Service 장애 시 재시도 가능한 서비스 불가 응답을 반환한다.
     *
     * @return 503 오류 응답
     */
    @RequestMapping("/fallback/contents")
    public Mono<ResponseEntity<Map<String, Object>>> contentsFallback() {
        return unavailable("CONTENTS_SERVICE_UNAVAILABLE", "콘텐츠 서비스를 일시적으로 사용할 수 없습니다.");
    }

    private Mono<ResponseEntity<Map<String, Object>>> unavailable(String code, String message) {
        Map<String, Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                "code", code,
                "message", message);
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body));
    }
}
