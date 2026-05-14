package com.example.websample.global.security.jwt;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** 등록된 인증 주체 로더 중 토큰의 principalType에 맞는 로더를 찾아 위임합니다. */
@Component
public class JwtPrincipalResolver {

    private final Map<String, JwtPrincipalLoader> loaders;

    public JwtPrincipalResolver(List<JwtPrincipalLoader> loaders) {
        this.loaders = loaders.stream()
                .collect(Collectors.toUnmodifiableMap(JwtPrincipalLoader::supports, Function.identity()));
    }

    public Optional<JwtPrincipal> resolve(String principalType, Long principalId) {

        JwtPrincipalLoader loader = loaders.get(principalType);
        if (loader == null) {
            return Optional.empty();
        }
        return loader.load(principalId);
    }
}
