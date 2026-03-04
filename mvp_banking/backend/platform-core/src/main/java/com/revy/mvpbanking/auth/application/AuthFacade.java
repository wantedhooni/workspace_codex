package com.revy.mvpbanking.auth.application;

import com.revy.mvpbanking.auth.infrastructure.AuthUserPrincipal;
import com.revy.mvpbanking.auth.presentation.PrincipalProfileResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Component
public class AuthFacade {

    public PrincipalProfileResponse currentPrincipal() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUserPrincipal principal)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Authentication required");
        }

        return new PrincipalProfileResponse(
                principal.getPrincipalId(),
                principal.getUsername(),
                principal.getDisplayName(),
                principal.getPrincipalType(),
                principal.getAuthorities().stream().map(Object::toString).toList()
        );
    }
}
