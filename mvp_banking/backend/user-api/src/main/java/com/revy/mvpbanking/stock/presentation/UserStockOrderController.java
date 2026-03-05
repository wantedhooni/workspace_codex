package com.revy.mvpbanking.stock.presentation;

import com.revy.mvpbanking.common.api.ApiResponse;
import com.revy.mvpbanking.common.support.CurrentPrincipalProvider;
import com.revy.mvpbanking.stock.application.StockOrderService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("user-api")
@RequestMapping("/api/user/stock-orders")
public class UserStockOrderController {

    private final StockOrderService stockOrderService;
    private final CurrentPrincipalProvider currentPrincipalProvider;

    public UserStockOrderController(StockOrderService stockOrderService, CurrentPrincipalProvider currentPrincipalProvider) {
        this.stockOrderService = stockOrderService;
        this.currentPrincipalProvider = currentPrincipalProvider;
    }

    @GetMapping
    public ApiResponse<List<StockOrderResponse>> list() {
        var principal = currentPrincipalProvider.getCurrentPrincipal();
        return ApiResponse.ok(stockOrderService.getUserOrders(principal.getPrincipalId()).stream().map(StockOrderResponse::from).toList());
    }

    @PostMapping
    public ApiResponse<StockOrderResponse> create(@Valid @RequestBody CreateStockOrderRequest request) {
        var principal = currentPrincipalProvider.getCurrentPrincipal();
        return ApiResponse.ok(StockOrderResponse.from(
                stockOrderService.create(
                        principal.getPrincipalId(),
                        request.accountId(),
                        request.symbol(),
                        request.market(),
                        request.side(),
                        request.quantity(),
                        request.limitPrice(),
                        request.currency(),
                        request.orderMemo(),
                        request.timeInForce()
                )
        ));
    }

    @PostMapping("/{orderId}/cancel")
    public ApiResponse<StockOrderResponse> cancel(
            @PathVariable UUID orderId,
            @Valid @RequestBody(required = false) CancelStockOrderRequest request
    ) {
        var principal = currentPrincipalProvider.getCurrentPrincipal();
        String reason = request == null ? null : request.reason();
        return ApiResponse.ok(StockOrderResponse.from(
                stockOrderService.cancelByUser(principal.getPrincipalId(), orderId, reason)
        ));
    }
}
