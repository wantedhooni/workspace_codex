package com.example.commerce.security;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Keycloak JWT의 scope, realm role, client role을 Spring Security 권한으로 변환한다.
 */
public class KeycloakJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final String clientId;

    /**
     * client role을 읽을 Keycloak 클라이언트 식별자를 받아 변환기를 생성한다.
     *
     * @param clientId Keycloak resource_access에서 조회할 클라이언트 식별자
     */
    public KeycloakJwtGrantedAuthoritiesConverter(String clientId) {
        this.clientId = clientId;
    }

    /**
     * JWT claim을 중복 없는 Spring Security 권한 목록으로 변환한다.
     *
     * @param jwt Keycloak이 발급한 JWT
     * @return Spring Security 권한 목록
     */
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<String> authorities = new LinkedHashSet<>();
        addScopes(jwt, authorities);
        addRealmRoles(jwt, authorities);
        addClientRoles(jwt, authorities);
        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }

    private void addScopes(Jwt jwt, Set<String> authorities) {
        Object scope = jwt.getClaims().get("scope");
        if (scope instanceof String scopeText) {
            for (String value : scopeText.split(" ")) {
                addAuthority(authorities, "SCOPE_", value);
            }
        }
        Object scp = jwt.getClaims().get("scp");
        if (scp instanceof Collection<?> values) {
            values.forEach(value -> addAuthority(authorities, "SCOPE_", value));
        }
    }

    private void addRealmRoles(Jwt jwt, Set<String> authorities) {
        Object realmAccess = jwt.getClaims().get("realm_access");
        if (realmAccess instanceof Map<?, ?> access) {
            addRoleClaims(access.get("roles"), authorities);
        }
    }

    private void addClientRoles(Jwt jwt, Set<String> authorities) {
        Object resourceAccess = jwt.getClaims().get("resource_access");
        if (resourceAccess instanceof Map<?, ?> resources
                && resources.get(clientId) instanceof Map<?, ?> clientAccess) {
            addRoleClaims(clientAccess.get("roles"), authorities);
        }
    }

    private void addRoleClaims(Object roles, Set<String> authorities) {
        if (roles instanceof Collection<?> values) {
            values.forEach(value -> addAuthority(authorities, "ROLE_", value));
        }
    }

    private void addAuthority(Set<String> authorities, String prefix, Object rawValue) {
        String value = String.valueOf(rawValue == null ? "" : rawValue).trim();
        if (!value.isBlank()) {
            authorities.add(prefix + value);
        }
    }
}
