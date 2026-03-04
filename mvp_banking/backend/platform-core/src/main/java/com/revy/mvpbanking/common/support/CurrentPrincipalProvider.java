package com.revy.mvpbanking.common.support;

import com.revy.mvpbanking.auth.infrastructure.AuthUserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Component
public class CurrentPrincipalProvider {

    public AuthUserPrincipal getCurrentPrincipal() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUserPrincipal principal)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Authentication required");
        }
        return principal;
    }
}
