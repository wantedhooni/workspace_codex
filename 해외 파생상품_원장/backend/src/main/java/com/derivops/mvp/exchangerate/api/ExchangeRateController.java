package com.derivops.mvp.exchangerate.api;

import com.derivops.mvp.exchangerate.application.ExchangeRateService;
import com.derivops.mvp.exchangerate.dto.ExchangeRateQuoteResponse;
import com.derivops.mvp.exchangerate.dto.ExchangeRateResponse;
import com.derivops.mvp.exchangerate.dto.UpsertExchangeRateRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
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
@RequestMapping("/api/v1/exchange-rates")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @PreAuthorize("hasAnyRole('OPS_ADMIN','OPS_VIEWER','AUDITOR')")
    @GetMapping
    public Page<ExchangeRateResponse> list(
            @RequestParam(required = false) String fromCurrency,
            @RequestParam(required = false) String toCurrency,
            @RequestParam(required = false) LocalDate rateDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return exchangeRateService.list(fromCurrency, toCurrency, rateDate, PageRequest.of(page, size));
    }

    @PreAuthorize("hasAnyRole('OPS_ADMIN','OPS_VIEWER','AUDITOR')")
    @GetMapping("/quote")
    public ExchangeRateQuoteResponse quote(
            @RequestParam String fromCurrency,
            @RequestParam String toCurrency,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) LocalDate rateDate
    ) {
        return exchangeRateService.quote(fromCurrency, toCurrency, amount, rateDate);
    }

    @PreAuthorize("hasRole('OPS_ADMIN')")
    @PostMapping
    public ExchangeRateResponse upsert(@Valid @RequestBody UpsertExchangeRateRequest request) {
        return exchangeRateService.upsert(request);
    }
}
