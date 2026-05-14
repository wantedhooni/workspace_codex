package com.example.websample.domain.user;

import com.example.websample.global.security.jwt.DefaultJwtPrincipal;
import com.example.websample.global.security.jwt.JwtPrincipal;

/** User 엔티티를 JWT 인증 모듈의 인증 주체로 변환하는 어댑터입니다. */
public final class UserJwtPrincipal {

    public static final String TYPE = "USER";

    private UserJwtPrincipal() {
    }

    public static JwtPrincipal from(User user) {
        return new DefaultJwtPrincipal(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                TYPE
        );
    }
}
