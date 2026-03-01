package com.derivops.mvp.opscase.api;
import com.derivops.mvp.opscase.*;
import com.derivops.mvp.opscase.application.*;
import com.derivops.mvp.opscase.dto.*;
import com.derivops.mvp.opscase.infrastructure.*;


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
@RequestMapping("/api/v1/ops-cases")
public class OpsCaseController {

    private final OpsCaseService opsCaseService;

    @PreAuthorize("hasAnyRole('OPS_ADMIN','OPS_VIEWER','AUDITOR')")
    @GetMapping
    public Page<OpsCaseResponse> list(
            @RequestParam(required = false) OpsCaseStatus status,
            @RequestParam(required = false) OpsCaseSeverity severity,
            @RequestParam(required = false) String assignee,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        return opsCaseService.list(status, severity, assignee, keyword, filter, PageRequest.of(page, size));
    }

    @PreAuthorize("hasAnyRole('OPS_ADMIN','OPS_VIEWER','AUDITOR')")
    @GetMapping("/{id}")
    public OpsCaseResponse get(@PathVariable Long id) {
        return opsCaseService.get(id);
    }

    @PreAuthorize("hasRole('OPS_ADMIN')")
    @PostMapping
    public OpsCaseResponse create(@Valid @RequestBody CreateOpsCaseRequest request) {
        return opsCaseService.create(request, SecurityUtils.currentUsername());
    }

    @PreAuthorize("hasRole('OPS_ADMIN')")
    @PutMapping("/{id}")
    public OpsCaseResponse update(@PathVariable Long id, @Valid @RequestBody UpdateOpsCaseRequest request) {
        return opsCaseService.update(id, request, SecurityUtils.currentUsername());
    }

    @PreAuthorize("hasRole('OPS_ADMIN')")
    @PostMapping("/{id}/start")
    public OpsCaseResponse start(@PathVariable Long id, @Valid @RequestBody TransitionOpsCaseRequest request) {
        return opsCaseService.start(id, request, SecurityUtils.currentUsername());
    }

    @PreAuthorize("hasRole('OPS_ADMIN')")
    @PostMapping("/{id}/resolve")
    public OpsCaseResponse resolve(@PathVariable Long id, @Valid @RequestBody TransitionOpsCaseRequest request) {
        return opsCaseService.resolve(id, request, SecurityUtils.currentUsername());
    }

    @PreAuthorize("hasRole('OPS_ADMIN')")
    @PostMapping("/{id}/close")
    public OpsCaseResponse close(@PathVariable Long id, @Valid @RequestBody TransitionOpsCaseRequest request) {
        return opsCaseService.close(id, request, SecurityUtils.currentUsername());
    }

    @PreAuthorize("hasRole('OPS_ADMIN')")
    @PostMapping("/{id}/reopen")
    public OpsCaseResponse reopen(@PathVariable Long id, @Valid @RequestBody TransitionOpsCaseRequest request) {
        return opsCaseService.reopen(id, request, SecurityUtils.currentUsername());
    }

    @PreAuthorize("hasRole('OPS_ADMIN')")
    @PostMapping("/{id}/assign")
    public OpsCaseResponse assign(@PathVariable Long id, @Valid @RequestBody AssignOpsCaseRequest request) {
        return opsCaseService.assign(id, request, SecurityUtils.currentUsername());
    }
}
