package com.quant.portal.api.presentation.controller;

import com.quant.portal.api.application.service.UserMenuPermissionService;
import com.quant.portal.api.presentation.dto.ApiListResponse;
import com.quant.portal.api.presentation.dto.userpermission.UserMenuPermissionResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me/menu-permissions")
public class UserMenuPermissionController {

    private final UserMenuPermissionService userMenuPermissionService;

    public UserMenuPermissionController(UserMenuPermissionService userMenuPermissionService) {
        this.userMenuPermissionService = userMenuPermissionService;
    }

    @GetMapping
    public ResponseEntity<ApiListResponse<UserMenuPermissionResponse>> list(Authentication authentication) {
        List<String> roleCodes = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        List<UserMenuPermissionResponse> data = userMenuPermissionService.findByRoleCodes(roleCodes);
        return ResponseEntity.ok(new ApiListResponse<>(data, data.size()));
    }
}
