package com.quant.portal.api.application.mapper;

import com.quant.portal.api.presentation.dto.CurrentUserResponse;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public final class CurrentUserResponseMapper {

    private CurrentUserResponseMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static CurrentUserResponse toDto(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .toList();

        return new CurrentUserResponse(authentication.getName(), roles);
    }
}
