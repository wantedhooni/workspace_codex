package com.quant.portal.api.presentation.controller;

import com.quant.portal.api.application.mapper.PortfolioMapper;
import com.quant.portal.api.application.query.PageableFactory;
import com.quant.portal.api.application.service.PortfolioPerformanceService;
import com.quant.portal.api.application.service.PortfolioService;
import com.quant.portal.api.presentation.dto.ApiListResponse;
import com.quant.portal.api.presentation.dto.ApiSingleResponse;
import com.quant.portal.api.presentation.dto.portfolio.PortfolioCreateRequest;
import com.quant.portal.api.presentation.dto.portfolio.PortfolioResponse;
import com.quant.portal.api.presentation.dto.portfolio.PortfolioUpdateRequest;
import com.quant.portal.api.presentation.filter.FilterParamParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.quant.portal.domain.portfolio.entity.Portfolio;
import com.quant.portal.domain.portfolio.enums.CurrencyCode;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final PortfolioPerformanceService portfolioPerformanceService;

    public PortfolioController(
            PortfolioService portfolioService,
            PortfolioPerformanceService portfolioPerformanceService
    ) {
        this.portfolioService = portfolioService;
        this.portfolioPerformanceService = portfolioPerformanceService;
    }

    @PostMapping
    public ResponseEntity<ApiSingleResponse<PortfolioResponse>> create(@Valid @RequestBody PortfolioCreateRequest request) {
        Portfolio portfolio = portfolioService.create(request);
        PortfolioResponse response = PortfolioMapper.toDto(portfolio, portfolioPerformanceService.calculate(portfolio));
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiSingleResponse<PortfolioResponse>> get(@PathVariable Long id) {
        Portfolio portfolio = portfolioService.get(id);
        PortfolioResponse response = PortfolioMapper.toDto(portfolio, portfolioPerformanceService.calculate(portfolio));
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }

    @GetMapping
    public ResponseEntity<ApiListResponse<PortfolioResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CurrencyCode baseCurrency,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String range,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int perPage,
            @RequestParam(required = false) String sort
    ) {
        JsonNode filterNode = FilterParamParser.parse(filter);
        String resolvedKeyword = keyword != null ? keyword : FilterParamParser.text(filterNode, "keyword");
        CurrencyCode resolvedBaseCurrency = baseCurrency != null ? baseCurrency
                : FilterParamParser.enumValue(filterNode, "baseCurrency", CurrencyCode.class);

        Pageable pageable = PageableFactory.portfolioPageable(page, perPage, sort, range);
        Page<Portfolio> result = portfolioService.search(resolvedKeyword, resolvedBaseCurrency, pageable);
        List<PortfolioResponse> data = result.getContent().stream()
                .map(portfolio -> PortfolioMapper.toDto(portfolio, portfolioPerformanceService.calculate(portfolio)))
                .toList();
        return ResponseEntity.ok(new ApiListResponse<>(data, result.getTotalElements()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiSingleResponse<PortfolioResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody PortfolioUpdateRequest request
    ) {
        Portfolio portfolio = portfolioService.update(id, request);
        PortfolioResponse response = PortfolioMapper.toDto(portfolio, portfolioPerformanceService.calculate(portfolio));
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        portfolioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
