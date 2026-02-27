package com.quant.portal.api.presentation.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.quant.portal.api.application.mapper.AdminUserMapper;
import com.quant.portal.api.application.query.PageableFactory;
import com.quant.portal.api.application.service.AdminUserService;
import com.quant.portal.api.presentation.dto.ApiListResponse;
import com.quant.portal.api.presentation.dto.ApiSingleResponse;
import com.quant.portal.api.presentation.dto.adminuser.AdminUserCreateRequest;
import com.quant.portal.api.presentation.dto.adminuser.AdminUserResponse;
import com.quant.portal.api.presentation.dto.adminuser.AdminUserUpdateRequest;
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
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @PostMapping
    public ResponseEntity<ApiSingleResponse<AdminUserResponse>> create(
            @Valid @RequestBody AdminUserCreateRequest request
    ) {
        AdminUserResponse response = AdminUserMapper.toDto(adminUserService.create(request));
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiSingleResponse<AdminUserResponse>> get(@PathVariable Long id) {
        AdminUserResponse response = AdminUserMapper.toDto(adminUserService.get(id));
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }

    @GetMapping
    public ResponseEntity<ApiListResponse<AdminUserResponse>> list(
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

        Pageable pageable = PageableFactory.adminUserPageable(page, perPage, sort, range);
        Page<AdminUserResponse> result = adminUserService.search(
                        resolvedKeyword,
                        resolvedRoleCode,
                        pageable
                )
                .map(AdminUserMapper::toDto);

        List<AdminUserResponse> data = result.getContent();
        return ResponseEntity.ok(new ApiListResponse<>(data, result.getTotalElements()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiSingleResponse<AdminUserResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserUpdateRequest request
    ) {
        AdminUserResponse response = AdminUserMapper.toDto(adminUserService.update(id, request));
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        adminUserService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

