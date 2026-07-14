package com.example.commerce.gateway.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * 인증된 JWT 또는 OAuth2 Client 로그인 세션의 access token을 다운스트림 서비스 Authorization 헤더로 전달한다.
 */
@Component
@ConditionalOnProperty(name = "commerce.security.oauth2.client-enabled", havingValue = "true")
public class OAuth2AccessTokenRelayGlobalFilter implements GlobalFilter, Ordered {

    private final ServerOAuth2AuthorizedClientRepository authorizedClientRepository;

    /**
     * OAuth2 authorized client 저장소를 주입받아 토큰 릴레이 필터를 생성한다.
     *
     * @param authorizedClientRepository WebSession 기반 OAuth2 authorized client 저장소
     */
    public OAuth2AccessTokenRelayGlobalFilter(
            ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
        this.authorizedClientRepository = authorizedClientRepository;
    }

    /**
     * 인증 객체에서 access token을 확인해 다운스트림 요청에 Bearer 헤더를 명시적으로 추가한다.
     *
     * @param exchange 현재 Gateway 교환 객체
     * @param chain 다음 Gateway 필터 체인
     * @return 필터 처리 결과
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .filter(Authentication.class::isInstance)
                .cast(Authentication.class)
                .flatMap(authentication -> resolveAccessToken(exchange, authentication))
                .map(token -> exchange.mutate()
                        .request(request -> request.headers(headers -> headers.setBearerAuth(token)))
                        .build())
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }

    /**
     * 인증 유형에 맞는 access token 값을 반환한다.
     *
     * @param exchange 현재 Gateway 교환 객체
     * @param authentication 현재 인증 객체
     * @return access token 값
     */
    private Mono<String> resolveAccessToken(ServerWebExchange exchange, Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return Mono.just(jwtAuthentication.getToken().getTokenValue());
        }
        if (authentication instanceof OAuth2AuthenticationToken oauth2Authentication) {
            return loadAccessToken(exchange, oauth2Authentication);
        }
        return Mono.empty();
    }

    /**
     * 현재 OAuth2 로그인 인증에서 authorized client를 찾아 access token 값을 반환한다.
     *
     * @param exchange 현재 Gateway 교환 객체
     * @param authentication OAuth2 로그인 인증
     * @return access token 값
     */
    private Mono<String> loadAccessToken(
            ServerWebExchange exchange,
            OAuth2AuthenticationToken authentication) {
        return authorizedClientRepository
                .<OAuth2AuthorizedClient>loadAuthorizedClient(
                        authentication.getAuthorizedClientRegistrationId(),
                        authentication,
                        exchange)
                .map(client -> client.getAccessToken().getTokenValue());
    }

    /**
     * 인증 필터 이후, 라우팅 필터 이전에 실행되도록 순서를 지정한다.
     *
     * @return 필터 실행 순서
     */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 100;
    }
}
