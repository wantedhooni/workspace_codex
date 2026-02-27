package com.quant.portal.api.application.mapper;

import com.quant.portal.api.presentation.dto.adminuser.AdminUserResponse;
import com.quant.portal.domain.admin.entity.AdminUser;
import java.util.Set;

public final class AdminUserMapper {

    private AdminUserMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static AdminUser toEntity(
            String username,
            String encodedPassword,
            String displayName,
            boolean enabled,
            Set<String> roleCodes
    ) {
        if (username == null || encodedPassword == null || displayName == null || roleCodes == null) {
            return null;
        }
        return new AdminUser(username, encodedPassword, displayName, enabled, roleCodes);
    }

    public static AdminUserResponse toDto(AdminUser adminUser) {
        if (adminUser == null) {
            return null;
        }
        return new AdminUserResponse(
                adminUser.getId(),
                adminUser.getUsername(),
                adminUser.getDisplayName(),
                adminUser.isEnabled(),
                adminUser.getRoleCodes().stream().sorted().toList()
        );
    }
}

