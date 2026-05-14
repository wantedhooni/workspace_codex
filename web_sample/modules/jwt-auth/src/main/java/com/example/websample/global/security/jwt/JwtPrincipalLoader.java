package com.example.websample.global.security.jwt;

import java.util.Optional;

/** JWT subject 값을 실제 인증 주체로 복원하는 도메인 어댑터 계약입니다. */
public interface JwtPrincipalLoader {

    String supports();

    Optional<JwtPrincipal> load(Long principalId);
}
