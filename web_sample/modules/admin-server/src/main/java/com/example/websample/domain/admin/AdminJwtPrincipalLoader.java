package com.example.websample.domain.admin;

import com.example.websample.global.security.jwt.JwtPrincipal;
import com.example.websample.global.security.jwt.JwtPrincipalLoader;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** JWT 토큰의 ADMIN 주체를 Admin 엔티티 기반 인증 주체로 복원합니다. */
@Component
public class AdminJwtPrincipalLoader implements JwtPrincipalLoader {

    private final AdminRepository adminRepository;

    public AdminJwtPrincipalLoader(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    public String supports() {
        return AdminJwtPrincipal.TYPE;
    }

    @Override
    public Optional<JwtPrincipal> load(Long principalId) {
        return adminRepository.findById(principalId).map(AdminJwtPrincipal::from);
    }
}
