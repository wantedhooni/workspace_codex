package com.example.samplegoogleoauth.auth.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * OAuth 로그인과 프론트엔드 리다이렉트에 필요한 애플리케이션 설정을 관리한다.
 */
@Validated
@ConfigurationProperties(prefix = "app.auth")
public record AppAuthProperties(
    @NotBlank String frontendUrl,
    @NotBlank String loginSuccessPath,
    @NotBlank String loginFailurePath
) {

    public String successRedirectUrl() {
        return join(frontendUrl, loginSuccessPath);
    }

    public String failureRedirectUrl() {
        return join(frontendUrl, loginFailurePath);
    }

    private String join(String baseUrl, String path) {
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }

        String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return normalizedBase + normalizedPath;
    }
}
