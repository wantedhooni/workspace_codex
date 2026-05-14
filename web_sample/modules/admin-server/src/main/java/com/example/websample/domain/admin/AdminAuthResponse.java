package com.example.websample.domain.admin;

/** 관리자 인증 성공 시 클라이언트에 반환하는 토큰과 관리자 정보입니다. */
public record AdminAuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        AdminSummary admin
) {
    public static AdminAuthResponse of(String accessToken, String refreshToken, Admin admin) {
        return new AdminAuthResponse(accessToken, refreshToken, "Bearer", AdminSummary.from(admin));
    }

    public record AdminSummary(Long id, String email, String name, String role) {
        public static AdminSummary from(Admin admin) {
            return new AdminSummary(admin.getId(), admin.getEmail(), admin.getName(), admin.getRole());
        }
    }
}
