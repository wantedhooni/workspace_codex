package com.tradingmacro.menu;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu-permissions")
public class MenuPermissionController {
    private final MenuPermissionRepository repository;
    private final MenuRepository menuRepository;

    public MenuPermissionController(MenuPermissionRepository repository, MenuRepository menuRepository) {
        this.repository = repository;
        this.menuRepository = menuRepository;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<MenuPermission> list() {
        return repository.findAll();
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public MenuPermission create(@Valid @RequestBody MenuPermissionRequest request) {
        MenuPermission permission = new MenuPermission();
        applyRequest(permission, request);
        return repository.save(permission);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public MenuPermission update(@PathVariable Long id, @Valid @RequestBody MenuPermissionRequest request) {
        MenuPermission permission = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Menu permission not found"));
        applyRequest(permission, request);
        return repository.save(permission);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    private void applyRequest(MenuPermission permission, MenuPermissionRequest request) {
        permission.setRole(request.role());
        permission.setMenu(menuRepository.findById(request.menuId())
            .orElseThrow(() -> new IllegalArgumentException("Menu not found")));
        permission.setCanView(request.canView());
        permission.setCanEdit(request.canEdit());
    }
}
