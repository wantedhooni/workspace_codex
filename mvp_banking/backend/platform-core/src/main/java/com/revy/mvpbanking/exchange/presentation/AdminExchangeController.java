package com.revy.mvpbanking.exchange.presentation;

import com.revy.mvpbanking.common.api.ApiResponse;
import com.revy.mvpbanking.exchange.application.ExchangeService;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("admin-api")
@RequestMapping("/api/admin/exchange-requests")
public class AdminExchangeController {

    private final ExchangeService exchangeService;

    public AdminExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @GetMapping
    public ApiResponse<List<ExchangeRequestResponse>> list() {
        return ApiResponse.ok(exchangeService.getAdminRequests().stream().map(ExchangeRequestResponse::from).toList());
    }
}
