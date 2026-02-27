package com.quant.portal.api.application.service;

import com.quant.portal.api.application.exception.ApiException;
import com.quant.portal.api.application.exception.ErrorCode;
import com.quant.portal.api.application.mapper.AdminUserMapper;
import com.quant.portal.api.application.query.AdminUserSearchCondition;
import com.quant.portal.api.infrastructure.jpa.repository.AdminUserRepository;
import com.quant.portal.api.presentation.dto.adminuser.AdminUserCreateRequest;
import com.quant.portal.api.presentation.dto.adminuser.AdminUserUpdateRequest;
import com.quant.portal.domain.admin.entity.AdminUser;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {

    private static final Set<String> ALLOWED_ROLE_CODES = Set.of("ROLE_ADMIN", "ROLE_USER");
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(
            AdminUserRepository adminUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AdminUser create(AdminUserCreateRequest request) {
        String username = normalizeUsername(request.username());
        String displayName = normalizeDisplayName(request.displayName());
        Set<String> roleCodes = normalizeRoleCodes(request.roleCodes());
        boolean enabled = request.enabled() == null || request.enabled();
        String rawPassword = normalizePassword(request.password(), "password");

        adminUserRepository.findByUsername(username).ifPresent(existing -> {
            throw new ApiException(HttpStatus.CONFLICT, ErrorCode.CONFLICT, "Username already exists");
        });

        AdminUser adminUser = AdminUserMapper.toEntity(
                username,
                passwordEncoder.encode(rawPassword),
                displayName,
                enabled,
                roleCodes
        );
        return adminUserRepository.save(adminUser);
    }

    @Transactional(readOnly = true)
    public AdminUser get(Long id) {
        return adminUserRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "User not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<AdminUser> search(String keyword, String roleCode, Pageable pageable) {
        AdminUserSearchCondition condition = new AdminUserSearchCondition(
                normalizeNullable(keyword),
                normalizeRoleCodeNullable(roleCode)
        );
        return adminUserRepository.search(condition, pageable);
    }

    @Transactional
    public AdminUser update(Long id, AdminUserUpdateRequest request) {
        AdminUser adminUser = get(id);
        Set<String> nextRoleCodes = normalizeRoleCodes(request.roleCodes());
        ensureLastAdminNotRemoved(adminUser, nextRoleCodes);

        adminUser.updateProfile(normalizeDisplayName(request.displayName()), request.enabled());
        adminUser.updateRoles(nextRoleCodes);

        String newPassword = normalizePasswordNullable(request.newPassword(), "newPassword");
        if (newPassword != null) {
            adminUser.changePassword(passwordEncoder.encode(newPassword));
        }

        return adminUser;
    }

    @Transactional
    public void delete(Long id) {
        AdminUser adminUser = get(id);
        ensureLastAdminNotDeleted(adminUser);
        adminUserRepository.delete(adminUser);
    }

    private void ensureLastAdminNotDeleted(AdminUser adminUser) {
        if (!adminUser.getRoleCodes().contains(ROLE_ADMIN)) {
            return;
        }
        long adminCount = adminUserRepository.countByRoleCode(ROLE_ADMIN);
        if (adminCount <= 1) {
            throw new ApiException(HttpStatus.CONFLICT, ErrorCode.CONFLICT, "At least one ROLE_ADMIN user is required");
        }
    }

    private void ensureLastAdminNotRemoved(AdminUser adminUser, Set<String> nextRoleCodes) {
        boolean currentlyAdmin = adminUser.getRoleCodes().contains(ROLE_ADMIN);
        boolean keepsAdmin = nextRoleCodes.contains(ROLE_ADMIN);
        if (!currentlyAdmin || keepsAdmin) {
            return;
        }
        long adminCount = adminUserRepository.countByRoleCode(ROLE_ADMIN);
        if (adminCount <= 1) {
            throw new ApiException(HttpStatus.CONFLICT, ErrorCode.CONFLICT, "At least one ROLE_ADMIN user is required");
        }
    }

    private static String normalizeUsername(String value) {
        String normalized = normalizeNullable(value);
        if (normalized == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "username must not be blank", details("username", "must not be blank"));
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    private static String normalizeDisplayName(String value) {
        String normalized = normalizeNullable(value);
        if (normalized == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "displayName must not be blank", details("displayName", "must not be blank"));
        }
        return normalized;
    }

    private static String normalizeRoleCodeNullable(String value) {
        String normalized = normalizeNullable(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private static Set<String> normalizeRoleCodes(List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "roleCodes must not be empty", details("roleCodes", "must not be empty"));
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String roleCode : roleCodes) {
            String value = normalizeRoleCodeNullable(roleCode);
            if (value == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "roleCodes contains blank value", details("roleCodes", "contains blank value"));
            }
            if (!ALLOWED_ROLE_CODES.contains(value)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "Unsupported roleCode: " + value, details("roleCodes", "contains unsupported roleCode: " + value));
            }
            normalized.add(value);
        }
        return normalized;
    }

    private static String normalizePassword(String value, String fieldName) {
        String normalized = normalizeNullable(value);
        if (normalized == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, fieldName + " must not be blank", details(fieldName, "must not be blank"));
        }
        if (normalized.length() < 8) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, fieldName + " must be at least 8 characters", details(fieldName, "must be at least 8 characters"));
        }
        return normalized;
    }

    private static String normalizePasswordNullable(String value, String fieldName) {
        String normalized = normalizeNullable(value);
        if (normalized == null) {
            return null;
        }
        if (normalized.length() < 8) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, fieldName + " must be at least 8 characters", details(fieldName, "must be at least 8 characters"));
        }
        return normalized;
    }

    private static String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static Map<String, String> details(String field, String message) {
        Map<String, String> detail = new LinkedHashMap<>();
        detail.put(field, message);
        return detail;
    }
}

