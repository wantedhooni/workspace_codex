package com.example.multitenancy.service;

import com.example.multitenancy.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * SecurityContext에서 현재 로그인 사용자를 꺼낸다.
 */
@Component
public class CurrentUserService {

    public AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
            throw new IllegalStateException("현재 로그인 사용자 정보를 찾을 수 없습니다.");
        }
        return authenticatedUser;
    }
}
