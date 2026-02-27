package com.quant.portal.api.presentation.controller;

import com.quant.portal.api.application.mapper.InstrumentMapper;
import com.quant.portal.api.application.query.PageableFactory;
import com.quant.portal.api.application.service.InstrumentService;
import com.quant.portal.api.presentation.dto.ApiListResponse;
import com.quant.portal.api.presentation.dto.ApiSingleResponse;
import com.quant.portal.api.presentation.dto.instrument.InstrumentCreateRequest;
import com.quant.portal.api.presentation.dto.instrument.InstrumentResponse;
import com.quant.portal.api.presentation.dto.instrument.InstrumentUpdateRequest;
import com.quant.portal.api.presentation.filter.FilterParamParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.quant.portal.domain.portfolio.enums.CurrencyCode;
import com.quant.portal.domain.portfolio.enums.MarketCode;
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
@RequestMapping("/api/v1/instruments")
public class InstrumentController {

    private final InstrumentService instrumentService;

    public InstrumentController(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    @PostMapping
    public ResponseEntity<ApiSingleResponse<InstrumentResponse>> create(@Valid @RequestBody InstrumentCreateRequest request) {
        InstrumentResponse response = InstrumentMapper.toDto(instrumentService.create(request));
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiSingleResponse<InstrumentResponse>> get(@PathVariable Long id) {
        InstrumentResponse response = InstrumentMapper.toDto(instrumentService.get(id));
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }

    @GetMapping
    public ResponseEntity<ApiListResponse<InstrumentResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) MarketCode marketCode,
            @RequestParam(required = false) CurrencyCode currencyCode,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String range,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int perPage,
            @RequestParam(required = false) String sort
    ) {
        JsonNode filterNode = FilterParamParser.parse(filter);
        String resolvedKeyword = keyword != null ? keyword : FilterParamParser.text(filterNode, "keyword");
        MarketCode resolvedMarketCode = marketCode != null ? marketCode
                : FilterParamParser.enumValue(filterNode, "marketCode", MarketCode.class);
        CurrencyCode resolvedCurrencyCode = currencyCode != null ? currencyCode
                : FilterParamParser.enumValue(filterNode, "currencyCode", CurrencyCode.class);

        Pageable pageable = PageableFactory.instrumentPageable(page, perPage, sort, range);
        Page<InstrumentResponse> result = instrumentService.search(
                        resolvedKeyword,
                        resolvedMarketCode,
                        resolvedCurrencyCode,
                        pageable
                )
                .map(InstrumentMapper::toDto);
        List<InstrumentResponse> data = result.getContent();
        return ResponseEntity.ok(new ApiListResponse<>(data, result.getTotalElements()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiSingleResponse<InstrumentResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody InstrumentUpdateRequest request
    ) {
        InstrumentResponse response = InstrumentMapper.toDto(instrumentService.update(id, request));
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        instrumentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
