package com.quant.mvp.api;

import com.quant.mvp.pipeline.domain.PermissionAction;
import com.quant.mvp.pipeline.payload.SavedViewDefaultPayload;
import com.quant.mvp.pipeline.payload.SavedViewPayload;
import com.quant.mvp.pipeline.service.AccessControlService;
import com.quant.mvp.pipeline.service.SavedViewService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/saved-views")
public class SavedViewController {

    private final SavedViewService savedViewService;
    private final PermissionGuard permissionGuard;
    private final AccessControlService accessControlService;

    public SavedViewController(
            SavedViewService savedViewService,
            PermissionGuard permissionGuard,
            AccessControlService accessControlService
    ) {
        this.savedViewService = savedViewService;
        this.permissionGuard = permissionGuard;
        this.accessControlService = accessControlService;
    }

    @GetMapping
    public SavedViewPayload.Res list(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestParam(required = false) String resourceKey
    ) {
        permissionGuard.require(userEmail, "savedViews", PermissionAction.READ);
        String actor = permissionGuard.resolveUserEmail(userEmail);
        return new SavedViewPayload.Res(
                savedViewService.search(resourceKey, actor).stream()
                        .map(this::toItem)
                        .collect(Collectors.toList())
        );
    }

    @PostMapping
    public SavedViewPayload.CreateRes create(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @Valid @RequestBody SavedViewPayload.Req req
    ) {
        permissionGuard.require(userEmail, "savedViews", PermissionAction.CREATE);
        String actor = permissionGuard.resolveUserEmail(userEmail);

        if (Boolean.TRUE.equals(req.shared()) && !canManageSharedViews(actor)) {
            throw new PermissionDeniedException("savedViews", PermissionAction.CREATE);
        }

        SavedViewService.SavedView created = savedViewService.create(
                req.resourceKey(),
                req.viewName(),
                req.description(),
                req.shared(),
                actor,
                req.filters()
        );
        return new SavedViewPayload.CreateRes(
                created.viewId(),
                created.resourceKey(),
                created.viewName(),
                created.description(),
                created.shared(),
                created.ownerEmail(),
                created.filters(),
                created.createdAt(),
                created.updatedAt()
        );
    }

    @DeleteMapping("/{viewId}")
    public SavedViewPayload.DeleteRes delete(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @PathVariable Long viewId
    ) {
        permissionGuard.require(userEmail, "savedViews", PermissionAction.DELETE);
        String actor = permissionGuard.resolveUserEmail(userEmail);
        SavedViewService.SavedView target = savedViewService.require(viewId);
        boolean adminOverride = canAdminOverride(actor);

        if (!adminOverride && !target.ownerEmail().equalsIgnoreCase(actor)) {
            throw new PermissionDeniedException("savedViews", PermissionAction.DELETE);
        }

        SavedViewService.SavedView deleted = savedViewService.delete(viewId, actor, adminOverride);
        return new SavedViewPayload.DeleteRes(
                deleted.viewId(),
                deleted.resourceKey(),
                deleted.viewName(),
                Instant.now()
        );
    }

    @GetMapping("/default")
    public SavedViewDefaultPayload.Res defaultView(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestParam String resourceKey
    ) {
        permissionGuard.require(userEmail, "savedViews", PermissionAction.READ);
        String actor = permissionGuard.resolveUserEmail(userEmail);
        Optional<SavedViewService.DefaultPinnedView> optional = savedViewService.getDefault(resourceKey, actor);
        if (optional.isEmpty()) {
            return new SavedViewDefaultPayload.Res(
                    resourceKey,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        SavedViewService.DefaultPinnedView pinned = optional.get();
        return new SavedViewDefaultPayload.Res(
                pinned.resourceKey(),
                pinned.viewId(),
                pinned.viewName(),
                pinned.shared(),
                pinned.ownerEmail(),
                pinned.pinnedAt()
        );
    }

    @PostMapping("/default")
    public SavedViewDefaultPayload.Res pinDefault(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @Valid @RequestBody SavedViewDefaultPayload.Req req
    ) {
        permissionGuard.require(userEmail, "savedViews", PermissionAction.READ);
        String actor = permissionGuard.resolveUserEmail(userEmail);
        SavedViewService.DefaultPinnedView pinned = savedViewService.pinDefault(
                req.resourceKey(),
                req.viewId(),
                actor
        );
        return new SavedViewDefaultPayload.Res(
                pinned.resourceKey(),
                pinned.viewId(),
                pinned.viewName(),
                pinned.shared(),
                pinned.ownerEmail(),
                pinned.pinnedAt()
        );
    }

    private SavedViewPayload.Item toItem(SavedViewService.SavedView view) {
        return new SavedViewPayload.Item(
                view.viewId(),
                view.resourceKey(),
                view.viewName(),
                view.description(),
                view.shared(),
                view.ownerEmail(),
                view.filters(),
                view.createdAt(),
                view.updatedAt()
        );
    }

    private boolean canManageSharedViews(String userEmail) {
        Set<String> roleCodes = roleCodesForUser(userEmail);
        return roleCodes.contains("ADMIN") || roleCodes.contains("RISK");
    }

    private boolean canAdminOverride(String userEmail) {
        return roleCodesForUser(userEmail).contains("ADMIN");
    }

    private Set<String> roleCodesForUser(String userEmail) {
        AccessControlService.UserView userView = accessControlService.getCurrentUser(userEmail);
        Set<String> roleCodes = new HashSet<>();
        userView.roleCodes().forEach(role -> roleCodes.add(role.toUpperCase(Locale.ROOT)));
        return roleCodes;
    }
}
