package com.quant.portal.api.config;

import com.quant.portal.api.application.mapper.AdminUserMapper;
import com.quant.portal.api.infrastructure.jpa.repository.AdminUserRepository;
import com.quant.portal.domain.admin.entity.AdminUser;
import java.util.Set;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SecurityUserBootstrap implements ApplicationRunner {

    private final AdminUserRepository adminUserRepository;
    private final SecurityProperties securityProperties;
    private final PasswordEncoder passwordEncoder;

    public SecurityUserBootstrap(
            AdminUserRepository adminUserRepository,
            SecurityProperties securityProperties,
            PasswordEncoder passwordEncoder
    ) {
        this.adminUserRepository = adminUserRepository;
        this.securityProperties = securityProperties;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        upsertDefaultUser(
                securityProperties.getAdminUsername(),
                securityProperties.getAdminPassword(),
                "Administrator",
                Set.of("ROLE_ADMIN", "ROLE_USER")
        );
        upsertDefaultUser(
                securityProperties.getUserUsername(),
                securityProperties.getUserPassword(),
                "Demo User",
                Set.of("ROLE_USER")
        );
    }

    private void upsertDefaultUser(
            String username,
            String rawPassword,
            String displayName,
            Set<String> roleCodes
    ) {
        if (username == null || username.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            return;
        }

        String normalizedUsername = username.trim().toLowerCase(java.util.Locale.ROOT);
        AdminUser existing = adminUserRepository.findByUsername(normalizedUsername).orElse(null);
        if (existing == null) {
            adminUserRepository.save(
                    AdminUserMapper.toEntity(
                            normalizedUsername,
                            passwordEncoder.encode(rawPassword),
                            displayName,
                            true,
                            roleCodes
                    )
            );
            return;
        }

        existing.updateProfile(displayName, true);
        existing.updateRoles(roleCodes);
        existing.changePassword(passwordEncoder.encode(rawPassword));
    }
}
