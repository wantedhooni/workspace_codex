package com.derivops.mvp.portfolio.api;

import com.derivops.mvp.portfolio.application.PortfolioService;
import com.derivops.mvp.portfolio.dto.PortfolioOverviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @PreAuthorize("hasAnyRole('OPS_ADMIN','OPS_VIEWER','AUDITOR')")
    @GetMapping("/{accountId}")
    public PortfolioOverviewResponse overview(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "false") boolean unmask
    ) {
        return portfolioService.getOverview(accountId, unmask);
    }
}
