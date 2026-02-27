package com.quant.portal.api.config;

import com.quant.portal.api.infrastructure.jpa.repository.AdminUserRepository;
import com.quant.portal.domain.admin.entity.AdminUser;
import java.util.Locale;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;

    public DatabaseUserDetailsService(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalizedUsername = normalizeUsername(username);
        AdminUser adminUser = adminUserRepository.findByUsername(normalizedUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + normalizedUsername));

        return User.withUsername(adminUser.getUsername())
                .password(adminUser.getPasswordHash())
                .authorities(adminUser.getRoleCodes().toArray(String[]::new))
                .disabled(!adminUser.isEnabled())
                .build();
    }

    private static String normalizeUsername(String username) {
        if (username == null) {
            return null;
        }
        return username.trim().toLowerCase(Locale.ROOT);
    }
}

