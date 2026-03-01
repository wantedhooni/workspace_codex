package com.derivops.mvp.stockpurchase.api;

import com.derivops.mvp.common.SecurityUtils;
import com.derivops.mvp.stockpurchase.application.StockPurchaseService;
import com.derivops.mvp.stockpurchase.dto.CreateStockPurchaseRequest;
import com.derivops.mvp.stockpurchase.dto.StockPurchaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/stock-purchases")
public class StockPurchaseController {

    private final StockPurchaseService stockPurchaseService;

    @PreAuthorize("hasAnyRole('OPS_ADMIN','OPS_VIEWER','AUDITOR')")
    @GetMapping
    public Page<StockPurchaseResponse> list(
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        return stockPurchaseService.list(accountId, symbol, keyword, filter, PageRequest.of(page, size));
    }

    @PreAuthorize("hasRole('OPS_ADMIN')")
    @PostMapping
    public StockPurchaseResponse create(@Valid @RequestBody CreateStockPurchaseRequest request) {
        return stockPurchaseService.create(request, SecurityUtils.currentUsername());
    }
}
