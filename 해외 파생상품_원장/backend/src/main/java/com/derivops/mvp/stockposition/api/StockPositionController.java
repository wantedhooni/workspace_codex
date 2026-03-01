package com.derivops.mvp.stockposition.api;

import com.derivops.mvp.stockposition.application.StockPositionService;
import com.derivops.mvp.stockposition.dto.StockPositionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/stock-positions")
public class StockPositionController {

    private final StockPositionService stockPositionService;

    @PreAuthorize("hasAnyRole('OPS_ADMIN','OPS_VIEWER','AUDITOR')")
    @GetMapping
    public Page<StockPositionResponse> list(
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        return stockPositionService.list(accountId, symbol, filter, PageRequest.of(page, size));
    }
}
