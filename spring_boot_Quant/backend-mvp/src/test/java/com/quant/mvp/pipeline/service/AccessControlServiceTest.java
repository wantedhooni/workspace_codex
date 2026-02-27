package com.quant.mvp.pipeline.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.quant.mvp.pipeline.domain.PermissionAction;
import com.quant.mvp.pipeline.domain.UserStatus;
import com.quant.mvp.pipeline.service.AuthenticationFailedException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AccessControlServiceTest {

    @Autowired
    private AccessControlService service;

    @Test
    void createUserAndManageStatusAndRoles() {
        AccessControlService.UserView created = service.createUser(
                "new.user@quant.io",
                "New User",
                UserStatus.ACTIVE,
                List.of("VIEWER")
        );

        assertEquals(UserStatus.ACTIVE, created.user().status());
        assertTrue(created.roleCodes().contains("VIEWER"));

        AccessControlService.UserView locked = service.updateUserStatus(created.user().userId(), UserStatus.LOCKED);
        assertEquals(UserStatus.LOCKED, locked.user().status());

        AccessControlService.UserView withRoles = service.assignUserRoles(created.user().userId(), List.of("VIEWER", "RISK"));
        assertEquals(2, withRoles.roleCodes().size());
        assertTrue(withRoles.roleCodes().contains("RISK"));
    }

    @Test
    void menuPermissionUpsertKeepsIdentity() {
        var first = service.upsertMenuPermission(1L, 1L, true, false, false, false);
        var second = service.upsertMenuPermission(1L, 1L, true, true, false, false);

        assertEquals(first.menuPermissionId(), second.menuPermissionId());
        assertNotEquals(first.updatedAt(), second.updatedAt());
        assertTrue(second.canCreate());
    }

    @Test
    void changePasswordValidatesCurrentAndRecentHistory() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.changeCurrentPassword("wrong-password", "Demo12345!")
        );

        service.changeCurrentPassword("demo1234", "Demo12345!");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.changeCurrentPassword("Demo12345!", "demo1234")
        );
    }

    @Test
    void revokeSessionMarksInactive() {
        Long activeSessionId = service.listCurrentSessions().stream()
                .filter(session -> session.active())
                .findFirst()
                .orElseThrow()
                .sessionId();

        var revoked = service.revokeCurrentSession(activeSessionId);
        assertEquals(false, revoked.active());
    }

    @Test
    void userMenusAreFilteredByRole() {
        List<AccessControlService.MenuAccessView> adminMenus = service.listCurrentUserMenus("admin@quant.io");
        List<AccessControlService.MenuAccessView> traderMenus = service.listCurrentUserMenus("trader@quant.io");
        List<AccessControlService.MenuAccessView> viewerMenus = service.listCurrentUserMenus("viewer@quant.io");

        assertTrue(adminMenus.stream().anyMatch(m -> m.menu().menuKey().equals("users")));
        assertTrue(traderMenus.stream().noneMatch(m -> m.menu().menuKey().equals("users")));
        assertTrue(traderMenus.stream().anyMatch(m -> m.menu().menuKey().equals("orders")));
        assertTrue(traderMenus.stream().anyMatch(m -> m.menu().menuKey().equals("orderAudits")));
        assertTrue(viewerMenus.stream().noneMatch(m -> m.menu().menuKey().equals("orders")));
        assertTrue(viewerMenus.stream().noneMatch(m -> m.menu().menuKey().equals("orderAudits")));
        assertTrue(viewerMenus.stream().anyMatch(m -> m.menu().menuKey().equals("accountProfile")));
    }

    @Test
    void menuPermissionCheckRespectsRoleAndAction() {
        assertTrue(service.hasMenuPermission("admin@quant.io", "users", PermissionAction.READ));
        assertTrue(service.hasMenuPermission("admin@quant.io", "users", PermissionAction.CREATE));
        assertFalse(service.hasMenuPermission("trader@quant.io", "users", PermissionAction.READ));
        assertTrue(service.hasMenuPermission("trader@quant.io", "orders", PermissionAction.READ));
        assertTrue(service.hasMenuPermission("trader@quant.io", "orders", PermissionAction.CREATE));
        assertFalse(service.hasMenuPermission("trader@quant.io", "orders", PermissionAction.UPDATE));
        assertTrue(service.hasMenuPermission("trader@quant.io", "orderAudits", PermissionAction.READ));
        assertFalse(service.hasMenuPermission("trader@quant.io", "orderAudits", PermissionAction.CREATE));
        assertFalse(service.hasMenuPermission("viewer@quant.io", "orders", PermissionAction.READ));
        assertFalse(service.hasMenuPermission("viewer@quant.io", "orderAudits", PermissionAction.READ));
        assertTrue(service.hasMenuPermission("trader@quant.io", "savedViews", PermissionAction.READ));
        assertTrue(service.hasMenuPermission("trader@quant.io", "savedViews", PermissionAction.CREATE));
        assertFalse(service.hasMenuPermission("viewer@quant.io", "savedViews", PermissionAction.CREATE));
        assertTrue(service.hasMenuPermission("risk@quant.io", "riskLimits", PermissionAction.UPDATE));
        assertTrue(service.hasMenuPermission("risk@quant.io", "orderAudits", PermissionAction.READ));
        assertFalse(service.hasMenuPermission("viewer@quant.io", "riskLimits", PermissionAction.READ));
    }

    @Test
    void authenticateValidatesCredentialsAndCreatesSession() {
        AccessControlService.AuthenticatedUser login = service.authenticate(
                "admin@quant.io",
                "demo1234",
                "127.0.0.1",
                "JUnit"
        );

        assertEquals("admin@quant.io", login.email());
        assertTrue(login.roleCodes().contains("ADMIN"));
        assertTrue(service.listCurrentSessions("admin@quant.io").stream()
                .anyMatch(session -> session.sessionId().equals(login.sessionId())));

        assertThrows(
                AuthenticationFailedException.class,
                () -> service.authenticate("admin@quant.io", "wrong-password", "127.0.0.1", "JUnit")
        );
    }

    @Test
    void deleteOperationsApplyBusinessConstraints() {
        AccessControlService.UserView user = service.createUser(
                "delete.user@quant.io",
                "Delete User",
                UserStatus.ACTIVE,
                List.of("VIEWER")
        );
        AccessControlService.UserView deletedUser = service.deleteUser(user.user().userId());
        assertEquals(user.user().userId(), deletedUser.user().userId());
        assertTrue(service.searchUsers(user.user().userId(), null, null, null, null).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> service.deleteUser(1L));

        var role = service.createRole("TEMP_DELETE_ROLE", "Temp Delete", "for test", false);
        var menu = service.createMenu(null, "tempDeleteMenu", "Temp Delete Menu", "/#/temp-delete", "delete", 999, true);
        var permission = service.upsertMenuPermission(menu.menuId(), role.roleId(), true, false, false, false);

        var deletedPermission = service.deleteMenuPermission(permission.menuPermissionId());
        assertEquals(permission.menuPermissionId(), deletedPermission.menuPermissionId());
        assertTrue(service.searchMenuPermissions(role.roleId(), menu.menuId(), null, null).isEmpty());

        var deletedMenu = service.deleteMenu(menu.menuId());
        assertEquals(menu.menuId(), deletedMenu.menuId());
        assertTrue(service.searchMenus(menu.menuId(), null, null, null, null).isEmpty());

        var deletedRole = service.deleteRole(role.roleId());
        assertEquals(role.roleId(), deletedRole.roleId());
        assertTrue(service.searchRoles(role.roleId(), null, null).isEmpty());

        assertThrows(IllegalArgumentException.class, () -> service.deleteRole(1L));
    }

    @Test
    void resetPasswordUsesTokenAndDisablesReuse() {
        AccessControlService.ResetPasswordResult issued = service.resetUserPassword(2L);
        assertEquals(2L, issued.userId());
        assertEquals(null, issued.temporaryPassword());
        assertTrue(issued.resetToken() != null && !issued.resetToken().isBlank());
        assertTrue(issued.expiresAt() != null);

        AccessControlService.PasswordResetConfirmResult confirmed =
                service.confirmPasswordReset(issued.resetToken(), "TraderStrong123!");
        assertTrue(confirmed.changed());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.confirmPasswordReset(issued.resetToken(), "AnotherStrong123!")
        );

        service.authenticate("trader@quant.io", "TraderStrong123!", "127.0.0.1", "JUnit");
        assertThrows(
                AuthenticationFailedException.class,
                () -> service.authenticate("trader@quant.io", "trader1234", "127.0.0.1", "JUnit")
        );
    }
}
