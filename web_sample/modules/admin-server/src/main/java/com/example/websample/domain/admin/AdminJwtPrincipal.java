package com.example.websample.domain.admin;

import com.example.websample.global.security.jwt.DefaultJwtPrincipal;
import com.example.websample.global.security.jwt.JwtPrincipal;

/** Admin 엔티티를 JWT 인증 모듈의 인증 주체로 변환하는 어댑터입니다. */
public final class AdminJwtPrincipal {

    public static final String TYPE = "ADMIN";

    private AdminJwtPrincipal() {
    }

    public static JwtPrincipal from(Admin admin) {
        return new DefaultJwtPrincipal(
                admin.getId(),
                admin.getEmail(),
                admin.getRole(),
                TYPE
        );
    }
}
