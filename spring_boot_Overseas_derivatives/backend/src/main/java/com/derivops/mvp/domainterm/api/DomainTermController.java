package com.derivops.mvp.domainterm.api;

import com.derivops.mvp.domainterm.application.DomainTermService;
import com.derivops.mvp.domainterm.dto.DomainTermResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/domain-terms")
public class DomainTermController {

    private final DomainTermService domainTermService;

    @PreAuthorize("hasAnyRole('OPS_ADMIN','OPS_VIEWER','AUDITOR')")
    @GetMapping
    public List<DomainTermResponse> list(@RequestParam(required = false) String domainKey) {
        return domainTermService.list(domainKey);
    }
}
