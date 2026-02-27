package com.quant.portal.api.presentation.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.quant.portal.api.application.mapper.MenuPermissionMapper;
import com.quant.portal.api.application.query.PageableFactory;
import com.quant.portal.api.application.service.MenuPermissionService;
import com.quant.portal.api.presentation.dto.ApiListResponse;
import com.quant.portal.api.presentation.dto.ApiSingleResponse;
import com.quant.portal.api.presentation.dto.menupermission.MenuPermissionCreateRequest;
import com.quant.portal.api.presentation.dto.menupermission.MenuPermissionResponse;
import com.quant.portal.api.presentation.dto.menupermission.MenuPermissionUpdateRequest;
import com.quant.portal.api.presentation.filter.FilterParamParser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/menu-permissions")
public class MenuPermissionAdminController {

    private final MenuPermissionService menuPermissionService;

    public MenuPermissionAdminController(MenuPermissionService menuPermissionService) {
        this.menuPermissionService = menuPermissionService;
    }

    @PostMapping
    public ResponseEntity<ApiSingleResponse<MenuPermissionResponse>> create(
            @Valid @RequestBody MenuPermissionCreateRequest request
    ) {
        MenuPermissionResponse response = MenuPermissionMapper.toDto(menuPermissionService.create(request));
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiSingleResponse<MenuPermissionResponse>> get(@PathVariable Long id) {
        MenuPermissionResponse response = MenuPermissionMapper.toDto(menuPermissionService.get(id));
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }

    @GetMapping
    public ResponseEntity<ApiListResponse<MenuPermissionResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String range,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int perPage,
            @RequestParam(required = false) String sort
    ) {
        JsonNode filterNode = FilterParamParser.parse(filter);
        String resolvedKeyword = keyword != null
                ? keyword
                : FilterParamParser.text(filterNode, "keyword");
        String resolvedRoleCode = roleCode != null
                ? roleCode
                : FilterParamParser.text(filterNode, "roleCode");

        Pageable pageable = PageableFactory.menuPermissionPageable(page, perPage, sort, range);
        Page<MenuPermissionResponse> result = menuPermissionService.search(
                        resolvedKeyword,
                        resolvedRoleCode,
                        pageable
                )
                .map(MenuPermissionMapper::toDto);
        List<MenuPermissionResponse> data = result.getContent();
        return ResponseEntity.ok(new ApiListResponse<>(data, result.getTotalElements()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiSingleResponse<MenuPermissionResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody MenuPermissionUpdateRequest request
    ) {
        MenuPermissionResponse response = MenuPermissionMapper.toDto(menuPermissionService.update(id, request));
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        menuPermissionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
