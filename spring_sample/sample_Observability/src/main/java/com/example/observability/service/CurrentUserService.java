package com.example.observability.service;

import com.example.observability.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * 현재 인증 사용자의 테넌트와 권한 정보를 조회한다.
 */
@Service
public class CurrentUserService {

    /**
     * 현재 요청의 인증 사용자 정보를 반환한다.
     */
    public AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
            throw new IllegalStateException("인증 사용자 정보를 찾을 수 없습니다.");
        }
        return authenticatedUser;
    }
}
