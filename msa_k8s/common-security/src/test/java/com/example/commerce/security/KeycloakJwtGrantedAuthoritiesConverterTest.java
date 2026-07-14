package com.example.commerce.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class KeycloakJwtGrantedAuthoritiesConverterTest {

    @Test
    void convertsScopeRealmRolesAndClientRoles() {
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(60),
                Map.of("alg", "none"),
                Map.of(
                        "scope", "openid profile",
                        "realm_access", Map.of("roles", List.of("commerce-user")),
                        "resource_access", Map.of(
                                "commerce-api", Map.of("roles", List.of("commerce-account")))));

        KeycloakJwtGrantedAuthoritiesConverter converter =
                new KeycloakJwtGrantedAuthoritiesConverter("commerce-api");

        assertThat(converter.convert(jwt))
                .extracting("authority")
                .containsExactly(
                        "SCOPE_openid",
                        "SCOPE_profile",
                        "ROLE_commerce-user",
                        "ROLE_commerce-account");
    }
}
