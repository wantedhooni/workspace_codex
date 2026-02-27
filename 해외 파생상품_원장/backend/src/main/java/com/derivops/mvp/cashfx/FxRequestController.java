package com.derivops.mvp.cashfx;

import com.derivops.mvp.common.SecurityUtils;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class FxRequestController {

    private final RequestService requestService;

    @PreAuthorize("hasRole('OPS_ADMIN')")
    @PostMapping("/fx-requests")
    public RequestResponse create(@Valid @RequestBody CreateFxRequest request) {
        return requestService.createFxRequest(request, SecurityUtils.currentUsername());
    }

    @PreAuthorize("hasAnyRole('OPS_ADMIN','OPS_VIEWER','AUDITOR')")
    @GetMapping("/fx-requests")
    public Page<RequestResponse> list(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return requestService.listFxRequests(status, accountId, keyword, filter, PageRequest.of(page, size));
    }

    @PreAuthorize("hasAnyRole('OPS_ADMIN','OPS_VIEWER','AUDITOR')")
    @GetMapping("/fx-requests/{requestId}")
    public RequestResponse get(@PathVariable UUID requestId) {
        return requestService.getFxRequest(requestId);
    }

    @PreAuthorize("hasRole('OPS_ADMIN')")
    @PostMapping("/requests/{requestId}/approve")
    public RequestResponse approve(@PathVariable UUID requestId, @Valid @RequestBody DecisionRequest request) {
        return requestService.approve(requestId, request.reason(), SecurityUtils.currentUsername());
    }

    @PreAuthorize("hasRole('OPS_ADMIN')")
    @PostMapping("/requests/{requestId}/reject")
    public RequestResponse reject(@PathVariable UUID requestId, @Valid @RequestBody DecisionRequest request) {
        return requestService.reject(requestId, request.reason(), SecurityUtils.currentUsername());
    }
}
