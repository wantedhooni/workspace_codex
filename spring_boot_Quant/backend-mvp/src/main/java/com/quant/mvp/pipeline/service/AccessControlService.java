package com.quant.mvp.pipeline.service;

import com.quant.mvp.access.application.mapper.AccessAccountSessionMapper;
import com.quant.mvp.access.application.mapper.AccessMenuMapper;
import com.quant.mvp.access.application.mapper.AccessMenuPermissionMapper;
import com.quant.mvp.access.application.mapper.AccessRoleMapper;
import com.quant.mvp.access.application.mapper.AccessUserMapper;
import com.quant.mvp.access.domain.AccessAccountSession;
import com.quant.mvp.access.domain.AccessMenu;
import com.quant.mvp.access.domain.AccessMenuPermission;
import com.quant.mvp.access.domain.AccessPasswordHistory;
import com.quant.mvp.access.domain.AccessPasswordResetToken;
import com.quant.mvp.access.domain.AccessRole;
import com.quant.mvp.access.domain.AccessUser;
import com.quant.mvp.access.domain.AccessUserRole;
import com.quant.mvp.access.infrastructure.AccessAccountSessionRepository;
import com.quant.mvp.access.infrastructure.AccessMenuPermissionRepository;
import com.quant.mvp.access.infrastructure.AccessMenuRepository;
import com.quant.mvp.access.infrastructure.AccessPasswordHistoryRepository;
import com.quant.mvp.access.infrastructure.AccessPasswordResetTokenRepository;
import com.quant.mvp.access.infrastructure.AccessRoleRepository;
import com.quant.mvp.access.infrastructure.AccessUserRepository;
import com.quant.mvp.access.infrastructure.AccessUserRoleRepository;
import com.quant.mvp.pipeline.domain.AccountSession;
import com.quant.mvp.pipeline.domain.Menu;
import com.quant.mvp.pipeline.domain.MenuPermission;
import com.quant.mvp.pipeline.domain.PermissionAction;
import com.quant.mvp.pipeline.domain.Role;
import com.quant.mvp.pipeline.domain.UserAccount;
import com.quant.mvp.pipeline.domain.UserStatus;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AccessControlService {

    private static final Long DEFAULT_CURRENT_USER_ID = 1L;
    private static final int PASSWORD_HISTORY_LIMIT = 5;
    private static final Duration RESET_TOKEN_TTL = Duration.ofMinutes(30);

    private final AccessUserRepository userRepository;
    private final AccessRoleRepository roleRepository;
    private final AccessMenuRepository menuRepository;
    private final AccessMenuPermissionRepository menuPermissionRepository;
    private final AccessUserRoleRepository userRoleRepository;
    private final AccessAccountSessionRepository accountSessionRepository;
    private final AccessPasswordHistoryRepository passwordHistoryRepository;
    private final AccessPasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public AccessControlService(
            AccessUserRepository userRepository,
            AccessRoleRepository roleRepository,
            AccessMenuRepository menuRepository,
            AccessMenuPermissionRepository menuPermissionRepository,
            AccessUserRoleRepository userRoleRepository,
            AccessAccountSessionRepository accountSessionRepository,
            AccessPasswordHistoryRepository passwordHistoryRepository,
            AccessPasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.menuRepository = menuRepository;
        this.menuPermissionRepository = menuPermissionRepository;
        this.userRoleRepository = userRoleRepository;
        this.accountSessionRepository = accountSessionRepository;
        this.passwordHistoryRepository = passwordHistoryRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserView> searchUsers(Long userId, String email, String name, String status, String roleCode) {
        UserStatus normalizedStatus = parseUserStatus(status);
        List<AccessUser> users = userRepository.searchUsers(userId, email, name, normalizedStatus, roleCode);
        if (users.isEmpty()) {
            return List.of();
        }

        Map<Long, List<String>> roleCodesByUserId = roleCodesByUserIds(
                users.stream().map(AccessUser::getId).collect(Collectors.toSet())
        );

        return users.stream()
                .map(user -> new UserView(
                        AccessUserMapper.toDomain(user),
                        roleCodesByUserId.getOrDefault(user.getId(), List.of())
                ))
                .toList();
    }

    @Transactional
    public UserView createUser(String email, String name, UserStatus status, List<String> roleCodes) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedName = requireText(name, "name");
        UserStatus normalizedStatus = status == null ? UserStatus.ACTIVE : status;
        Set<Long> roleIds = resolveRoleIds(roleCodes);

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("email already exists: " + normalizedEmail);
        }

        AccessUser user = AccessUser.create(
                normalizedEmail,
                normalizedName,
                normalizedStatus,
                passwordEncoder.encode("Welcome123!")
        );
        AccessUser created = userRepository.save(user);

        saveUserRoles(created.getId(), roleIds);
        appendPasswordHistory(created.getId(), created.getPasswordHash());
        createSession(created.getId(), "127.0.0.1", "Local Demo Browser");

        return new UserView(AccessUserMapper.toDomain(created), roleCodesForUser(created.getId()));
    }

    @Transactional
    public UserView updateUserStatus(Long userId, UserStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status is required");
        }

        AccessUser user = requireUser(userId);
        user.updateStatus(status);
        AccessUser updated = userRepository.save(user);
        return new UserView(AccessUserMapper.toDomain(updated), roleCodesForUser(updated.getId()));
    }

    @Transactional
    public UserView assignUserRoles(Long userId, List<String> roleCodes) {
        AccessUser user = requireUser(userId);
        Set<Long> roleIds = resolveRoleIds(roleCodes);
        saveUserRoles(user.getId(), roleIds);
        return new UserView(AccessUserMapper.toDomain(user), roleCodesForUser(user.getId()));
    }

    @Transactional
    public UserView deleteUser(Long userId) {
        if (Objects.equals(userId, DEFAULT_CURRENT_USER_ID)) {
            throw new IllegalArgumentException("default admin user cannot be deleted");
        }

        AccessUser user = requireUser(userId);
        UserView deleted = new UserView(AccessUserMapper.toDomain(user), roleCodesForUser(userId));

        userRoleRepository.deleteByUserId(userId);
        accountSessionRepository.deleteByUserId(userId);
        passwordHistoryRepository.deleteByUserId(userId);
        passwordResetTokenRepository.deleteByUserId(userId);
        userRepository.delete(user);

        return deleted;
    }

    @Transactional
    public ResetPasswordResult resetUserPassword(Long userId) {
        AccessUser user = requireUser(userId);
        Instant now = Instant.now();

        invalidateActiveResetTokens(user.getId(), now);

        String resetToken = generateResetToken();
        AccessPasswordResetToken created = passwordResetTokenRepository.save(
                AccessPasswordResetToken.create(user.getId(), hashToken(resetToken), now.plus(RESET_TOKEN_TTL))
        );

        return new ResetPasswordResult(user.getId(), null, now, resetToken, created.getExpiresAt());
    }

    @Transactional
    public PasswordResetConfirmResult confirmPasswordReset(String token, String newPassword) {
        String normalizedToken = requireText(token, "token");
        Instant now = Instant.now();

        AccessPasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(hashToken(normalizedToken))
                .orElseThrow(() -> new IllegalArgumentException("invalid password reset token"));

        if (!resetToken.isUsableAt(now)) {
            throw new IllegalArgumentException("password reset token is expired or already used");
        }

        AccessUser user = requireUser(resetToken.getUserId());
        validateNewPassword(user.getId(), user.getPasswordHash(), newPassword);

        String encoded = passwordEncoder.encode(requireText(newPassword, "newPassword"));
        user.updatePasswordHash(encoded);
        userRepository.save(user);

        appendPasswordHistory(user.getId(), encoded);
        resetToken.markUsed(now);
        passwordResetTokenRepository.save(resetToken);

        return new PasswordResetConfirmResult(user.getId(), true, now);
    }

    @Transactional
    public AuthenticatedUser authenticate(String email, String password, String ipAddress, String userAgent) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedPassword = requireText(password, "password");

        AccessUser user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new AuthenticationFailedException("invalid credentials"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AuthenticationFailedException("account is not active: " + user.getStatus());
        }

        if (!passwordEncoder.matches(normalizedPassword, user.getPasswordHash())) {
            throw new AuthenticationFailedException("invalid credentials");
        }

        Instant now = Instant.now();
        user.updateLastLoginAt(now);
        AccessUser loggedIn = userRepository.save(user);

        AccessAccountSession session = createSession(
                loggedIn.getId(),
                ipAddress == null || ipAddress.isBlank() ? "unknown" : ipAddress,
                userAgent == null || userAgent.isBlank() ? "unknown" : userAgent.trim()
        );

        return new AuthenticatedUser(
                loggedIn.getId(),
                loggedIn.getEmail(),
                loggedIn.getName(),
                roleCodesForUser(loggedIn.getId()),
                session.getId()
        );
    }

    public UserView getCurrentUser() {
        return getCurrentUser(null);
    }

    public UserView getCurrentUser(String userEmail) {
        Long userId = resolveCurrentUserId(userEmail);
        AccessUser user = requireUser(userId);
        return new UserView(AccessUserMapper.toDomain(user), roleCodesForUser(userId));
    }

    @Transactional
    public ChangePasswordResult changeCurrentPassword(String currentPassword, String newPassword) {
        return changeCurrentPassword(null, currentPassword, newPassword);
    }

    @Transactional
    public ChangePasswordResult changeCurrentPassword(
            String userEmail,
            String currentPassword,
            String newPassword
    ) {
        Long userId = resolveCurrentUserId(userEmail);
        AccessUser user = requireUser(userId);

        if (!passwordEncoder.matches(requireText(currentPassword, "currentPassword"), user.getPasswordHash())) {
            throw new IllegalArgumentException("currentPassword is incorrect");
        }

        validateNewPassword(userId, user.getPasswordHash(), newPassword);

        String encoded = passwordEncoder.encode(requireText(newPassword, "newPassword"));
        user.updatePasswordHash(encoded);
        userRepository.save(user);
        appendPasswordHistory(userId, encoded);

        return new ChangePasswordResult(userId, true, Instant.now());
    }

    public List<AccountSession> listCurrentSessions() {
        return listCurrentSessions(null);
    }

    public List<AccountSession> listCurrentSessions(String userEmail) {
        Long userId = resolveCurrentUserId(userEmail);
        return accountSessionRepository.findByUserIdOrderByIdAsc(userId).stream()
                .map(AccessAccountSessionMapper::toDomain)
                .toList();
    }

    @Transactional
    public AccountSession revokeCurrentSession(Long sessionId) {
        return revokeCurrentSession(null, sessionId);
    }

    @Transactional
    public AccountSession revokeCurrentSession(String userEmail, Long sessionId) {
        Long userId = resolveCurrentUserId(userEmail);

        AccessAccountSession session = accountSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("session not found: " + sessionId));

        if (!Objects.equals(session.getUserId(), userId)) {
            throw new IllegalArgumentException("cannot revoke another user's session");
        }

        session.revoke(Instant.now());
        return AccessAccountSessionMapper.toDomain(accountSessionRepository.save(session));
    }

    @Transactional
    public List<AccountSession> revokeOtherActiveSessions(String userEmail) {
        Long userId = resolveCurrentUserId(userEmail);

        List<AccessAccountSession> activeSessions = accountSessionRepository
                .findByUserIdAndActiveTrueOrderByLastAccessAtDescIdAsc(userId);

        if (activeSessions.size() <= 1) {
            return List.of();
        }

        AccessAccountSession keep = activeSessions.get(0);
        Instant now = Instant.now();
        List<AccessAccountSession> revoked = new ArrayList<>();
        for (AccessAccountSession session : activeSessions) {
            if (Objects.equals(session.getId(), keep.getId())) {
                continue;
            }
            session.revoke(now);
            revoked.add(session);
        }

        accountSessionRepository.saveAll(revoked);
        return revoked.stream()
                .map(AccessAccountSessionMapper::toDomain)
                .toList();
    }

    public List<MenuAccessView> listCurrentUserMenus() {
        return listCurrentUserMenus(null);
    }

    public List<MenuAccessView> listCurrentUserMenus(String userEmail) {
        Long userId = resolveCurrentUserId(userEmail);
        Set<Long> roleIds = roleIdsForUser(userId);
        if (roleIds.isEmpty()) {
            return List.of();
        }

        List<AccessMenuPermission> permissions = menuPermissionRepository.findByRoleIdIn(roleIds);
        if (permissions.isEmpty()) {
            return List.of();
        }

        Set<Long> menuIds = permissions.stream()
                .map(AccessMenuPermission::getMenuId)
                .collect(Collectors.toSet());

        Map<Long, Menu> menuById = menuRepository.findAllById(menuIds).stream()
                .filter(AccessMenu::isEnabled)
                .collect(Collectors.toMap(AccessMenu::getId, AccessMenuMapper::toDomain));

        Map<Long, PermissionAccumulator> byMenu = new HashMap<>();
        for (AccessMenuPermission permission : permissions) {
            Menu menu = menuById.get(permission.getMenuId());
            if (menu == null) {
                continue;
            }
            PermissionAccumulator accumulator = byMenu.computeIfAbsent(menu.menuId(), key -> new PermissionAccumulator(menu));
            accumulator.merge(permission);
        }

        return byMenu.values().stream()
                .map(PermissionAccumulator::toView)
                .filter(MenuAccessView::canRead)
                .sorted(Comparator.comparing((MenuAccessView view) -> view.menu().sortOrder())
                        .thenComparing(view -> view.menu().menuId()))
                .toList();
    }

    public List<Role> searchRoles(Long roleId, String roleCode, String roleName) {
        return roleRepository.searchRoles(roleId, roleCode, roleName).stream()
                .map(AccessRoleMapper::toDomain)
                .toList();
    }

    @Transactional
    public Role createRole(String roleCode, String roleName, String description, Boolean systemRole) {
        String normalizedRoleCode = requireText(roleCode, "roleCode").toUpperCase(Locale.ROOT);
        String normalizedRoleName = requireText(roleName, "roleName");
        String normalizedDescription = description == null ? "" : description.trim();

        if (roleRepository.existsByRoleCodeIgnoreCase(normalizedRoleCode)) {
            throw new IllegalArgumentException("roleCode already exists: " + normalizedRoleCode);
        }

        AccessRole role = AccessRole.create(
                normalizedRoleCode,
                normalizedRoleName,
                normalizedDescription,
                Boolean.TRUE.equals(systemRole)
        );

        return AccessRoleMapper.toDomain(roleRepository.save(role));
    }

    @Transactional
    public Role deleteRole(Long roleId) {
        AccessRole role = requireRole(roleId);
        if (role.isSystemRole()) {
            throw new IllegalArgumentException("system role cannot be deleted: " + role.getRoleCode());
        }

        if (userRoleRepository.existsByRoleId(roleId)) {
            throw new IllegalArgumentException("role is assigned to users: " + role.getRoleCode());
        }

        menuPermissionRepository.deleteByRoleId(roleId);
        roleRepository.delete(role);
        return AccessRoleMapper.toDomain(role);
    }

    public List<Menu> searchMenus(Long menuId, Long parentMenuId, String menuKey, String menuLabel, Boolean enabled) {
        return menuRepository.searchMenus(menuId, parentMenuId, menuKey, menuLabel, enabled).stream()
                .map(AccessMenuMapper::toDomain)
                .toList();
    }

    @Transactional
    public Menu createMenu(
            Long parentMenuId,
            String menuKey,
            String menuLabel,
            String path,
            String icon,
            Integer sortOrder,
            Boolean enabled
    ) {
        if (parentMenuId != null && !menuRepository.existsById(parentMenuId)) {
            throw new IllegalArgumentException("parentMenu not found: " + parentMenuId);
        }

        String normalizedMenuKey = requireText(menuKey, "menuKey");
        String normalizedMenuLabel = requireText(menuLabel, "menuLabel");
        String normalizedPath = requireText(path, "path");
        String normalizedIcon = icon == null ? "view_list" : icon.trim();
        Integer normalizedSortOrder = sortOrder == null ? 999 : sortOrder;

        if (menuRepository.existsByMenuKeyIgnoreCase(normalizedMenuKey)) {
            throw new IllegalArgumentException("menuKey already exists: " + normalizedMenuKey);
        }

        AccessMenu created = menuRepository.save(AccessMenu.create(
                parentMenuId,
                normalizedMenuKey,
                normalizedMenuLabel,
                normalizedPath,
                normalizedIcon,
                normalizedSortOrder,
                enabled == null || enabled
        ));

        return AccessMenuMapper.toDomain(created);
    }

    @Transactional
    public Menu deleteMenu(Long menuId) {
        AccessMenu menu = requireMenu(menuId);
        if (menuRepository.existsByParentMenuId(menuId)) {
            throw new IllegalArgumentException("menu has child menus and cannot be deleted: " + menu.getMenuKey());
        }

        menuPermissionRepository.deleteByMenuId(menuId);
        menuRepository.delete(menu);
        return AccessMenuMapper.toDomain(menu);
    }

    public List<MenuPermissionView> searchMenuPermissions(Long roleId, Long menuId, String roleCode, String menuKey) {
        List<AccessMenuPermission> permissions = menuPermissionRepository.searchMenuPermissions(roleId, menuId, roleCode, menuKey);
        if (permissions.isEmpty()) {
            return List.of();
        }

        Set<Long> roleIds = permissions.stream().map(AccessMenuPermission::getRoleId).collect(Collectors.toSet());
        Set<Long> menuIds = permissions.stream().map(AccessMenuPermission::getMenuId).collect(Collectors.toSet());

        Map<Long, AccessRole> roleMap = roleRepository.findAllById(roleIds).stream()
                .collect(Collectors.toMap(AccessRole::getId, role -> role));
        Map<Long, AccessMenu> menuMap = menuRepository.findAllById(menuIds).stream()
                .collect(Collectors.toMap(AccessMenu::getId, menu -> menu));

        return permissions.stream()
                .map(permission -> {
                    AccessRole role = roleMap.get(permission.getRoleId());
                    AccessMenu menu = menuMap.get(permission.getMenuId());
                    return new MenuPermissionView(
                            AccessMenuPermissionMapper.toDomain(permission),
                            menu == null ? "unknown" : menu.getMenuKey(),
                            role == null ? "unknown" : role.getRoleCode()
                    );
                })
                .toList();
    }

    @Transactional
    public MenuPermission upsertMenuPermission(
            Long menuId,
            Long roleId,
            boolean canRead,
            boolean canCreate,
            boolean canUpdate,
            boolean canDelete
    ) {
        requireMenu(menuId);
        requireRole(roleId);

        AccessMenuPermission permission = menuPermissionRepository.findByMenuIdAndRoleId(menuId, roleId)
                .orElse(null);

        if (permission == null) {
            AccessMenuPermission created = menuPermissionRepository.save(
                    AccessMenuPermission.create(menuId, roleId, canRead, canCreate, canUpdate, canDelete)
            );
            return AccessMenuPermissionMapper.toDomain(created);
        }

        permission.update(canRead, canCreate, canUpdate, canDelete);
        return AccessMenuPermissionMapper.toDomain(menuPermissionRepository.save(permission));
    }

    @Transactional
    public MenuPermission deleteMenuPermission(Long menuPermissionId) {
        if (menuPermissionId == null || menuPermissionId <= 0) {
            throw new IllegalArgumentException("menuPermissionId must be positive");
        }

        AccessMenuPermission permission = menuPermissionRepository.findById(menuPermissionId)
                .orElseThrow(() -> new IllegalArgumentException("menuPermission not found: " + menuPermissionId));

        menuPermissionRepository.delete(permission);
        return AccessMenuPermissionMapper.toDomain(permission);
    }

    public Long resolveCurrentUserId(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            return DEFAULT_CURRENT_USER_ID;
        }

        String normalized = userEmail.trim().toLowerCase(Locale.ROOT);
        return userRepository.findByEmailIgnoreCase(normalized)
                .map(AccessUser::getId)
                .orElseThrow(() -> new IllegalArgumentException("user not found by email: " + userEmail));
    }

    public boolean hasMenuPermission(String userEmail, String menuKey, PermissionAction action) {
        if (menuKey == null || menuKey.isBlank()) {
            throw new IllegalArgumentException("menuKey is required");
        }
        if (action == null) {
            throw new IllegalArgumentException("action is required");
        }

        Long userId = resolveCurrentUserId(userEmail);
        Set<Long> roleIds = roleIdsForUser(userId);
        if (roleIds.isEmpty()) {
            return false;
        }

        AccessMenu menu = menuRepository.findByMenuKeyIgnoreCase(menuKey.trim())
                .filter(AccessMenu::isEnabled)
                .orElse(null);
        if (menu == null) {
            return false;
        }

        return menuPermissionRepository.findByMenuIdAndRoleIdIn(menu.getId(), roleIds).stream()
                .anyMatch(permission -> allows(permission, action));
    }

    private Set<Long> roleIdsForUser(Long userId) {
        return userRoleRepository.findByUserId(userId).stream()
                .map(AccessUserRole::getRoleId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Map<Long, List<String>> roleCodesByUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        List<AccessUserRole> userRoles = userRoleRepository.findByUserIdIn(userIds);
        if (userRoles.isEmpty()) {
            return Map.of();
        }

        Set<Long> roleIds = userRoles.stream().map(AccessUserRole::getRoleId).collect(Collectors.toSet());
        Map<Long, String> roleCodeByRoleId = roleRepository.findAllById(roleIds).stream()
                .collect(Collectors.toMap(AccessRole::getId, AccessRole::getRoleCode));

        Map<Long, List<String>> result = new HashMap<>();
        for (AccessUserRole userRole : userRoles) {
            String roleCode = roleCodeByRoleId.get(userRole.getRoleId());
            if (roleCode == null) {
                continue;
            }
            result.computeIfAbsent(userRole.getUserId(), key -> new ArrayList<>()).add(roleCode);
        }

        result.replaceAll((key, value) -> value.stream().sorted().toList());
        return result;
    }

    private List<String> roleCodesForUser(Long userId) {
        Set<Long> roleIds = roleIdsForUser(userId);
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return roleRepository.findAllById(roleIds).stream()
                .map(AccessRole::getRoleCode)
                .sorted()
                .toList();
    }

    private Set<Long> resolveRoleIds(List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            throw new IllegalArgumentException("roleCodes is required");
        }

        Set<Long> roleIds = new LinkedHashSet<>();
        for (String roleCode : roleCodes) {
            String normalizedRoleCode = requireText(roleCode, "roleCode").toUpperCase(Locale.ROOT);
            AccessRole role = roleRepository.findByRoleCodeIgnoreCase(normalizedRoleCode)
                    .orElseThrow(() -> new IllegalArgumentException("role not found: " + normalizedRoleCode));
            roleIds.add(role.getId());
        }

        return roleIds;
    }

    private UserStatus parseUserStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        try {
            return UserStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("invalid status: " + status + " (allowed: ACTIVE, LOCKED, INACTIVE)");
        }
    }

    private String normalizeEmail(String email) {
        String normalized = requireText(email, "email").toLowerCase(Locale.ROOT);
        if (!normalized.contains("@")) {
            throw new IllegalArgumentException("invalid email format");
        }
        return normalized;
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    private AccessUser requireUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId must be positive");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found: " + userId));
    }

    private AccessRole requireRole(Long roleId) {
        if (roleId == null || roleId <= 0) {
            throw new IllegalArgumentException("roleId must be positive");
        }

        return roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("role not found: " + roleId));
    }

    private AccessMenu requireMenu(Long menuId) {
        if (menuId == null || menuId <= 0) {
            throw new IllegalArgumentException("menuId must be positive");
        }

        return menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("menu not found: " + menuId));
    }

    private void saveUserRoles(Long userId, Set<Long> roleIds) {
        userRoleRepository.deleteByUserId(userId);
        userRoleRepository.flush();
        List<AccessUserRole> userRoles = roleIds.stream()
                .map(roleId -> AccessUserRole.create(userId, roleId))
                .toList();
        userRoleRepository.saveAll(userRoles);
    }

    private void validateNewPassword(Long userId, String currentPasswordHash, String newPassword) {
        String normalized = requireText(newPassword, "newPassword");
        if (normalized.length() < 8) {
            throw new IllegalArgumentException("newPassword length must be >= 8");
        }
        if (passwordEncoder.matches(normalized, currentPasswordHash)) {
            throw new IllegalArgumentException("newPassword must be different from currentPassword");
        }

        List<AccessPasswordHistory> history = passwordHistoryRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);
        for (AccessPasswordHistory item : history) {
            if (passwordEncoder.matches(normalized, item.getPasswordHash())) {
                throw new IllegalArgumentException("newPassword cannot reuse recent password");
            }
        }
    }

    private void appendPasswordHistory(Long userId, String passwordHash) {
        passwordHistoryRepository.save(AccessPasswordHistory.create(userId, passwordHash));

        List<AccessPasswordHistory> history = passwordHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (history.size() <= PASSWORD_HISTORY_LIMIT) {
            return;
        }

        List<AccessPasswordHistory> expired = history.subList(PASSWORD_HISTORY_LIMIT, history.size());
        passwordHistoryRepository.deleteAll(expired);
    }

    private AccessAccountSession createSession(Long userId, String ipAddress, String userAgent) {
        return accountSessionRepository.save(AccessAccountSession.create(
                userId,
                ipAddress,
                userAgent,
                true,
                Instant.now()
        ));
    }

    private void invalidateActiveResetTokens(Long userId, Instant now) {
        List<AccessPasswordResetToken> activeTokens = passwordResetTokenRepository.findByUserIdAndUsedAtIsNull(userId);
        if (activeTokens.isEmpty()) {
            return;
        }

        for (AccessPasswordResetToken token : activeTokens) {
            token.markUsed(now);
        }
        passwordResetTokenRepository.saveAll(activeTokens);
    }

    private String generateResetToken() {
        return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (Exception ex) {
            throw new IllegalStateException("failed to hash reset token", ex);
        }
    }

    private boolean allows(AccessMenuPermission permission, PermissionAction action) {
        return switch (action) {
            case READ -> permission.isCanRead();
            case CREATE -> permission.isCanCreate();
            case UPDATE -> permission.isCanUpdate();
            case DELETE -> permission.isCanDelete();
        };
    }

    public record UserView(UserAccount user, List<String> roleCodes) {
    }

    public record ResetPasswordResult(
            Long userId,
            String temporaryPassword,
            Instant resetAt,
            String resetToken,
            Instant expiresAt
    ) {
    }

    public record ChangePasswordResult(Long userId, boolean changed, Instant changedAt) {
    }

    public record PasswordResetConfirmResult(Long userId, boolean changed, Instant changedAt) {
    }

    public record AuthenticatedUser(
            Long userId,
            String email,
            String name,
            List<String> roleCodes,
            Long sessionId
    ) {
    }

    public record MenuPermissionView(MenuPermission permission, String menuKey, String roleCode) {
    }

    public record MenuAccessView(
            Menu menu,
            boolean canRead,
            boolean canCreate,
            boolean canUpdate,
            boolean canDelete
    ) {
    }

    private static final class PermissionAccumulator {

        private final Menu menu;
        private boolean canRead;
        private boolean canCreate;
        private boolean canUpdate;
        private boolean canDelete;

        private PermissionAccumulator(Menu menu) {
            this.menu = menu;
        }

        private void merge(AccessMenuPermission permission) {
            canRead = canRead || permission.isCanRead();
            canCreate = canCreate || permission.isCanCreate();
            canUpdate = canUpdate || permission.isCanUpdate();
            canDelete = canDelete || permission.isCanDelete();
        }

        private MenuAccessView toView() {
            return new MenuAccessView(menu, canRead, canCreate, canUpdate, canDelete);
        }
    }
}
