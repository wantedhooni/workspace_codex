package com.revy.mvpbanking.stock.presentation;

import com.revy.mvpbanking.common.api.ApiResponse;
import com.revy.mvpbanking.stock.application.StockOrderService;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("admin-api")
@RequestMapping("/api/admin/stock-orders")
public class AdminStockOrderController {

    private final StockOrderService stockOrderService;

    public AdminStockOrderController(StockOrderService stockOrderService) {
        this.stockOrderService = stockOrderService;
    }

    @GetMapping
    public ApiResponse<List<StockOrderResponse>> list() {
        return ApiResponse.ok(stockOrderService.getAdminOrders().stream().map(StockOrderResponse::from).toList());
    }

    @PostMapping("/{orderId}/complete-fill")
    public ApiResponse<Void> completeFill(@PathVariable UUID orderId) {
        stockOrderService.completeRemainingFill(orderId);
        return ApiResponse.ok(null);
    }
}
