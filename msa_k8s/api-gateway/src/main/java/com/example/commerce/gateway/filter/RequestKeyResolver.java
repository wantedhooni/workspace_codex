package com.example.commerce.gateway.filter;

import java.net.InetSocketAddress;
import java.security.Principal;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * 인증 주체 또는 직접 연결된 클라이언트 주소를 Gateway 요청 제한 키로 변환한다.
 */
@Component("requestKeyResolver")
public class RequestKeyResolver implements KeyResolver {

    private static final String UNKNOWN_CLIENT = "unknown";

    /**
     * 요청 제한 키 생성기를 생성한다.
     */
    public RequestKeyResolver() {
    }

    /**
     * 인증된 요청은 사용자명을, 비인증 요청은 원격 주소를 요청 제한 키로 사용한다.
     *
     * @param exchange 현재 서버 교환 객체
     * @return 요청 제한 키
     */
    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        return exchange.getPrincipal()
                .flatMap(this::principalName)
                .switchIfEmpty(Mono.fromSupplier(() -> remoteAddress(exchange)));
    }

    private Mono<String> principalName(Principal principal) {
        String name = principal.getName();
        if (name == null || name.isBlank()) {
            return Mono.empty();
        }
        return Mono.just(name);
    }

    private String remoteAddress(ServerWebExchange exchange) {
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress == null || remoteAddress.getAddress() == null) {
            return UNKNOWN_CLIENT;
        }
        return remoteAddress.getAddress().getHostAddress();
    }
}
