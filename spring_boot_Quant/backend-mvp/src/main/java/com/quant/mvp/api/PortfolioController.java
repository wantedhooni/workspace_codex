package com.quant.mvp.api;

import com.quant.mvp.pipeline.domain.PermissionAction;
import com.quant.mvp.pipeline.payload.PortfolioCatalogPayload;
import com.quant.mvp.pipeline.service.PortfolioCatalogService;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {

    private final PortfolioCatalogService portfolioCatalogService;
    private final PermissionGuard permissionGuard;

    public PortfolioController(
            PortfolioCatalogService portfolioCatalogService,
            PermissionGuard permissionGuard
    ) {
        this.portfolioCatalogService = portfolioCatalogService;
        this.permissionGuard = permissionGuard;
    }

    @GetMapping
    public PortfolioCatalogPayload.Res list(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean activeOnly
    ) {
        permissionGuard.require(userEmail, "portfolios", PermissionAction.READ);
        return new PortfolioCatalogPayload.Res(
                portfolioCatalogService.search(keyword, activeOnly).stream()
                        .map(row -> new PortfolioCatalogPayload.Item(
                                row.portfolioId(),
                                row.portfolioCode(),
                                row.portfolioName(),
                                row.strategyTag(),
                                row.benchmark(),
                                row.baseCurrency(),
                                row.active()
                        ))
                        .collect(Collectors.toList())
        );
    }
}
