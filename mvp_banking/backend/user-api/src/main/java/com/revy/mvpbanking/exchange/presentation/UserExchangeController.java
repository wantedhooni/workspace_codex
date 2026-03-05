package com.revy.mvpbanking.exchange.presentation;

import com.revy.mvpbanking.common.api.ApiResponse;
import com.revy.mvpbanking.common.support.CurrentPrincipalProvider;
import com.revy.mvpbanking.exchange.application.ExchangeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("user-api")
@RequestMapping("/api/user/exchange-requests")
public class UserExchangeController {

    private final ExchangeService exchangeService;
    private final CurrentPrincipalProvider currentPrincipalProvider;

    public UserExchangeController(ExchangeService exchangeService, CurrentPrincipalProvider currentPrincipalProvider) {
        this.exchangeService = exchangeService;
        this.currentPrincipalProvider = currentPrincipalProvider;
    }

    @GetMapping
    public ApiResponse<List<ExchangeRequestResponse>> list() {
        var principal = currentPrincipalProvider.getCurrentPrincipal();
        return ApiResponse.ok(exchangeService.getUserRequests(principal.getPrincipalId()).stream().map(ExchangeRequestResponse::from).toList());
    }

    @PostMapping
    public ApiResponse<ExchangeRequestResponse> create(@Valid @RequestBody CreateExchangeRequest request) {
        var principal = currentPrincipalProvider.getCurrentPrincipal();
        return ApiResponse.ok(ExchangeRequestResponse.from(
                exchangeService.create(
                        principal.getPrincipalId(),
                        request.sourceAccountId(),
                        request.destinationAccountId(),
                        request.fromAmount(),
                        request.requestMemo()
                )
        ));
    }

    @PostMapping("/{requestId}/cancel")
    public ApiResponse<ExchangeRequestResponse> cancel(
            @PathVariable UUID requestId,
            @Valid @RequestBody(required = false) CancelExchangeRequest request
    ) {
        var principal = currentPrincipalProvider.getCurrentPrincipal();
        String reason = request == null ? null : request.reason();
        return ApiResponse.ok(ExchangeRequestResponse.from(
                exchangeService.cancelByUser(principal.getPrincipalId(), requestId, reason)
        ));
    }
}
