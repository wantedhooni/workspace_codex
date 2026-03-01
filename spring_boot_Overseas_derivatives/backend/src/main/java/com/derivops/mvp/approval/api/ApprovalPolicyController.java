package com.derivops.mvp.approval.api;
import com.derivops.mvp.approval.*;
import com.derivops.mvp.approval.application.*;
import com.derivops.mvp.approval.dto.*;
import com.derivops.mvp.approval.infrastructure.*;


import com.derivops.mvp.common.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/approval-policies")
public class ApprovalPolicyController {

    private final ApprovalPolicyService approvalPolicyService;

    @PreAuthorize("hasAnyRole('OPS_ADMIN','AUDITOR')")
    @GetMapping
    public Page<ApprovalPolicyResponse> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        return approvalPolicyService.list(keyword, filter, PageRequest.of(page, size));
    }

    @PreAuthorize("hasRole('OPS_ADMIN')")
    @PostMapping
    public ApprovalPolicyResponse create(@Valid @RequestBody UpsertApprovalPolicyRequest request) {
        return approvalPolicyService.create(request, SecurityUtils.currentUsername());
    }

    @PreAuthorize("hasRole('OPS_ADMIN')")
    @PutMapping("/{id}")
    public ApprovalPolicyResponse update(@PathVariable Long id, @Valid @RequestBody UpsertApprovalPolicyRequest request) {
        return approvalPolicyService.update(id, request, SecurityUtils.currentUsername());
    }
}
