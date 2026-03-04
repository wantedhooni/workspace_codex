package com.revy.mvpbanking.approval.presentation;

import com.revy.mvpbanking.approval.application.ApprovalService;
import com.revy.mvpbanking.common.api.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("admin-api")
@RequestMapping("/api/admin/approvals")
public class AdminApprovalController {

    private final ApprovalService approvalService;

    public AdminApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @GetMapping
    public ApiResponse<List<ApprovalRequestResponse>> list() {
        return ApiResponse.ok(approvalService.getApprovalRequests().stream().map(ApprovalRequestResponse::from).toList());
    }

    @PostMapping("/{approvalRequestId}/approve")
    public ApiResponse<ApprovalRequestResponse> approve(
            @PathVariable("approvalRequestId") UUID approvalRequestId,
            @Valid @RequestBody ApprovalDecisionRequest request
    ) {
        return ApiResponse.ok(ApprovalRequestResponse.from(approvalService.approve(approvalRequestId, request.reason())));
    }

    @PostMapping("/{approvalRequestId}/reject")
    public ApiResponse<ApprovalRequestResponse> reject(
            @PathVariable("approvalRequestId") UUID approvalRequestId,
            @Valid @RequestBody ApprovalDecisionRequest request
    ) {
        return ApiResponse.ok(ApprovalRequestResponse.from(approvalService.reject(approvalRequestId, request.reason())));
    }
}
