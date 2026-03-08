package com.revy.mvpbanking.stock.presentation;

import com.revy.mvpbanking.common.api.ApiResponse;
import com.revy.mvpbanking.common.api.PageResponse;
import com.revy.mvpbanking.common.support.CurrentPrincipalProvider;
import com.revy.mvpbanking.stock.application.StockPositionService;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("user-api")
@RequestMapping("/api/user/stock-positions")
public class UserStockPositionController {

    private final StockPositionService stockPositionService;
    private final CurrentPrincipalProvider currentPrincipalProvider;

    public UserStockPositionController(
            StockPositionService stockPositionService,
            CurrentPrincipalProvider currentPrincipalProvider
    ) {
        this.stockPositionService = stockPositionService;
        this.currentPrincipalProvider = currentPrincipalProvider;
    }

    @GetMapping
    public ApiResponse<PageResponse<StockPositionResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var principal = currentPrincipalProvider.getCurrentPrincipal();
        return ApiResponse.ok(PageResponse.from(
                stockPositionService.getUserPositions(principal.getPrincipalId()).stream().map(StockPositionResponse::from).toList(),
                page,
                size
        ));
    }
}
