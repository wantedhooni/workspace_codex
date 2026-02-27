package com.quant.portal.api.presentation.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.quant.portal.api.config.SecurityConfig;
import com.quant.portal.api.config.SecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = SystemController.class)
@Import({SecurityConfig.class, SystemControllerSecurityTest.TestSecurityUserDetailsConfig.class})
@TestPropertySource(properties = {
        "app.security.admin-username=admin",
        "app.security.admin-password=admin1234",
        "app.security.user-username=user",
        "app.security.user-password=user1234",
        "app.cors.allowed-origins[0]=http://localhost:3000"
})
class SystemControllerSecurityTest {

    @TestConfiguration
    static class TestSecurityUserDetailsConfig {

        @Bean
        UserDetailsService userDetailsService(SecurityProperties properties, PasswordEncoder passwordEncoder) {
            UserDetails admin = User.builder()
                    .username(properties.getAdminUsername())
                    .password(passwordEncoder.encode(properties.getAdminPassword()))
                    .roles("ADMIN", "USER")
                    .build();

            UserDetails user = User.builder()
                    .username(properties.getUserUsername())
                    .password(passwordEncoder.encode(properties.getUserPassword()))
                    .roles("USER")
                    .build();

            return new InMemoryUserDetailsManager(admin, user);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAllowPublicPingWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/public/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("pong"));
    }

    @Test
    void shouldRejectUnauthenticatedUserForProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowUserToAccessOwnProfile() throws Exception {
        mockMvc.perform(get("/api/v1/users/me").with(httpBasic("user", "user1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("user"));
    }

    @Test
    void shouldBlockUserFromAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/admin/ping").with(httpBasic("user", "user1234")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminEndpointForAdminRole() throws Exception {
        mockMvc.perform(get("/api/v1/admin/ping").with(httpBasic("admin", "admin1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("admin-pong"));
    }

    @Test
    void shouldAllowCorsPreflightFromConfiguredOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/users/me")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }
}
