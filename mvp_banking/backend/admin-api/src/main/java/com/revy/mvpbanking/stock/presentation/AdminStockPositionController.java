package com.revy.mvpbanking.stock.presentation;

import com.revy.mvpbanking.common.api.ApiResponse;
import com.revy.mvpbanking.stock.application.StockPositionService;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("admin-api")
@RequestMapping("/api/admin/stock-positions")
public class AdminStockPositionController {

    private final StockPositionService stockPositionService;

    public AdminStockPositionController(StockPositionService stockPositionService) {
        this.stockPositionService = stockPositionService;
    }

    @GetMapping
    public ApiResponse<List<StockPositionResponse>> list() {
        return ApiResponse.ok(stockPositionService.getAdminPositions().stream().map(StockPositionResponse::from).toList());
    }
}
