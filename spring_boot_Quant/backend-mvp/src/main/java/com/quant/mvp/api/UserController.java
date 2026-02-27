package com.quant.mvp.api;

import com.quant.mvp.pipeline.payload.AssignUserRolesPayload;
import com.quant.mvp.pipeline.payload.CreateUserPayload;
import com.quant.mvp.pipeline.payload.DeleteUserPayload;
import com.quant.mvp.pipeline.payload.ResetUserPasswordPayload;
import com.quant.mvp.pipeline.payload.UpdateUserStatusPayload;
import com.quant.mvp.pipeline.payload.UserSearchPayload;
import com.quant.mvp.pipeline.domain.PermissionAction;
import com.quant.mvp.pipeline.service.AccessControlService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AccessControlService accessControlService;
    private final PermissionGuard permissionGuard;

    public UserController(AccessControlService accessControlService, PermissionGuard permissionGuard) {
        this.accessControlService = accessControlService;
        this.permissionGuard = permissionGuard;
    }

    @GetMapping
    public UserSearchPayload.Res list(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String roleCode
    ) {
        permissionGuard.require(userEmail, "users", PermissionAction.READ);
        return new UserSearchPayload.Res(
                accessControlService.searchUsers(userId, email, name, status, roleCode).stream()
                        .map(this::toItem)
                        .collect(Collectors.toList())
        );
    }

    @PostMapping
    public CreateUserPayload.Res create(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @Valid @RequestBody CreateUserPayload.Req req
    ) {
        permissionGuard.require(userEmail, "users", PermissionAction.CREATE);
        AccessControlService.UserView created = accessControlService.createUser(
                req.email(),
                req.name(),
                req.status(),
                req.roleCodes()
        );

        return new CreateUserPayload.Res(
                created.user().userId(),
                created.user().email(),
                created.user().name(),
                created.user().status(),
                created.roleCodes(),
                created.user().createdAt(),
                created.user().updatedAt()
        );
    }

    @PutMapping("/{userId}/status")
    public UpdateUserStatusPayload.Res updateStatus(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusPayload.Req req
    ) {
        permissionGuard.require(userEmail, "users", PermissionAction.UPDATE);
        AccessControlService.UserView updated = accessControlService.updateUserStatus(userId, req.status());
        return new UpdateUserStatusPayload.Res(
                updated.user().userId(),
                updated.user().status(),
                updated.user().updatedAt()
        );
    }

    @PutMapping("/{userId}/roles")
    public AssignUserRolesPayload.Res assignRoles(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @PathVariable Long userId,
            @Valid @RequestBody AssignUserRolesPayload.Req req
    ) {
        permissionGuard.require(userEmail, "users", PermissionAction.UPDATE);
        AccessControlService.UserView updated = accessControlService.assignUserRoles(userId, req.roleCodes());
        return new AssignUserRolesPayload.Res(
                updated.user().userId(),
                updated.roleCodes(),
                updated.user().updatedAt()
        );
    }

    @PostMapping("/{userId}/reset-password")
    public ResetUserPasswordPayload.Res resetPassword(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @PathVariable Long userId,
            @RequestBody(required = false) ResetUserPasswordPayload.Req req
    ) {
        permissionGuard.require(userEmail, "users", PermissionAction.UPDATE);
        AccessControlService.ResetPasswordResult resetResult = accessControlService.resetUserPassword(userId);
        return new ResetUserPasswordPayload.Res(
                resetResult.userId(),
                resetResult.temporaryPassword(),
                resetResult.resetAt(),
                resetResult.resetToken(),
                resetResult.expiresAt()
        );
    }

    @DeleteMapping("/{userId}")
    public DeleteUserPayload.Res delete(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @PathVariable Long userId
    ) {
        permissionGuard.require(userEmail, "users", PermissionAction.DELETE);
        AccessControlService.UserView deleted = accessControlService.deleteUser(userId);
        return new DeleteUserPayload.Res(
                deleted.user().userId(),
                deleted.user().email(),
                Instant.now()
        );
    }

    private UserSearchPayload.Item toItem(AccessControlService.UserView view) {
        return new UserSearchPayload.Item(
                view.user().userId(),
                view.user().email(),
                view.user().name(),
                view.user().status(),
                view.roleCodes(),
                view.user().lastLoginAt(),
                view.user().createdAt(),
                view.user().updatedAt()
        );
    }
}
