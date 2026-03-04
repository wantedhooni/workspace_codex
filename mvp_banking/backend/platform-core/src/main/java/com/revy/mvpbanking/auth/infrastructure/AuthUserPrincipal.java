package com.revy.mvpbanking.auth.infrastructure;

import com.revy.mvpbanking.auth.domain.AuthenticatedPrincipal;
import com.revy.mvpbanking.auth.domain.PrincipalType;
import java.util.Collection;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthUserPrincipal implements UserDetails {

    private final AuthenticatedPrincipal principal;

    public AuthUserPrincipal(AuthenticatedPrincipal principal) {
        this.principal = principal;
    }

    public UUID getPrincipalId() {
        return principal.id();
    }

    public PrincipalType getPrincipalType() {
        return principal.principalType();
    }

    public String getDisplayName() {
        return principal.displayName();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return principal.roles().stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return principal.email();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return principal.active();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return principal.active();
    }
}
