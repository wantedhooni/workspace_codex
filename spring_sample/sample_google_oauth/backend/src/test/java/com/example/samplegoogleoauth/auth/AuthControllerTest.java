package com.example.samplegoogleoauth.auth;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.samplegoogleoauth.auth.entity.OAuthMember;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import com.example.samplegoogleoauth.auth.repository.OAuthMemberRepository;
import com.example.samplegoogleoauth.auth.service.OAuthMemberSyncService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@SpringBootTest(properties = {
    "GOOGLE_CLIENT_ID=test-client-id",
    "GOOGLE_CLIENT_SECRET=test-client-secret",
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OAuthMemberRepository oauthMemberRepository;

    @Autowired
    private OAuthMemberSyncService oauthMemberSyncService;

    @Test
    @DisplayName("인증된 사용자는 현재 사용자 정보를 조회할 수 있다")
    void shouldReturnCurrentUser() throws Exception {
        oauthMemberRepository.save(new OAuthMember(
            "GOOGLE",
            "google-100",
            "hong@example.com",
            "홍길동",
            "https://example.com/profile.png"
        ));

        mockMvc.perform(get("/api/auth/me")
                .with(oauth2Login().attributes(attributes -> {
                    attributes.put("sub", "google-100");
                    attributes.put("name", "홍길동");
                    attributes.put("email", "hong@example.com");
                    attributes.put("picture", "https://example.com/profile.png");
                })))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.authenticated").value(true))
            .andExpect(jsonPath("$.registered").value(false))
            .andExpect(jsonPath("$.name").value("홍길동"))
            .andExpect(jsonPath("$.email").value("hong@example.com"))
            .andExpect(jsonPath("$.picture").value("https://example.com/profile.png"));
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 사용자 정보를 조회할 수 없다")
    void shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그아웃 API는 인증된 사용자를 종료한다")
    void shouldLogoutAuthenticatedUser() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .with(oauth2Login()))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("OAuth 로그인 사용자는 추가 정보를 입력해 회원가입을 완료할 수 있다")
    void shouldSignupOauthUser() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                .with(oauth2Login().attributes(attributes -> {
                    attributes.put("sub", "google-101");
                    attributes.put("name", "김예린");
                    attributes.put("email", "yerin@example.com");
                    attributes.put("picture", "https://example.com/yerin.png");
                }))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "displayName": "예린",
                      "organization": "플랫폼실",
                      "jobTitle": "백엔드 엔지니어",
                      "marketingConsent": true
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.registered").value(true))
            .andExpect(jsonPath("$.displayName").value("예린"))
            .andExpect(jsonPath("$.organization").value("플랫폼실"))
            .andExpect(jsonPath("$.jobTitle").value("백엔드 엔지니어"))
            .andExpect(jsonPath("$.marketingConsent").value(true));
    }

    @Test
    @DisplayName("OAuth 로그인 성공 시 백엔드 RDBMS에 기본 회원 정보가 저장된다")
    void shouldPersistMemberOnOauthLoginSuccess() {
        OAuth2User oauth2User = new DefaultOAuth2User(
            List.of(new SimpleGrantedAuthority("ROLE_USER")),
            Map.of(
                "sub", "google-777",
                "name", "박서준",
                "email", "seojun@example.com",
                "picture", "https://example.com/seojun.png"
            ),
            "sub"
        );

        Authentication authentication = new OAuth2AuthenticationToken(
            oauth2User,
            oauth2User.getAuthorities(),
            "google"
        );

        oauthMemberSyncService.syncOnLogin(authentication);

        OAuthMember member = oauthMemberRepository.findByProviderAndProviderUserId("GOOGLE", "google-777")
            .orElseThrow(() -> new AssertionError("로그인 직후 회원 정보가 저장되어야 합니다."));

        Assertions.assertEquals("seojun@example.com", member.getEmail());
        Assertions.assertFalse(member.isRegistered());
    }
}
