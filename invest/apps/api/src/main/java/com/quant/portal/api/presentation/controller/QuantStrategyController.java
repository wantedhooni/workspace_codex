package com.quant.portal.api.presentation.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.quant.portal.api.application.mapper.QuantStrategyMapper;
import com.quant.portal.api.application.query.PageableFactory;
import com.quant.portal.api.application.service.QuantStrategyService;
import com.quant.portal.api.presentation.dto.ApiListResponse;
import com.quant.portal.api.presentation.dto.ApiSingleResponse;
import com.quant.portal.api.presentation.dto.quantstrategy.QuantStrategyCreateRequest;
import com.quant.portal.api.presentation.dto.quantstrategy.QuantStrategyResponse;
import com.quant.portal.api.presentation.dto.quantstrategy.QuantStrategyUpdateRequest;
import com.quant.portal.api.presentation.filter.FilterParamParser;
import com.quant.portal.domain.quant.enums.QuantStyle;
import com.quant.portal.domain.quant.enums.StrategyStatus;
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
@RequestMapping("/api/v1/quant-strategies")
public class QuantStrategyController {

    private final QuantStrategyService quantStrategyService;

    public QuantStrategyController(QuantStrategyService quantStrategyService) {
        this.quantStrategyService = quantStrategyService;
    }

    @PostMapping
    public ResponseEntity<ApiSingleResponse<QuantStrategyResponse>> create(
            @Valid @RequestBody QuantStrategyCreateRequest request
    ) {
        QuantStrategyResponse response = QuantStrategyMapper.toDto(quantStrategyService.create(request));
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiSingleResponse<QuantStrategyResponse>> get(@PathVariable Long id) {
        QuantStrategyResponse response = QuantStrategyMapper.toDto(quantStrategyService.get(id));
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }

    @GetMapping
    public ResponseEntity<ApiListResponse<QuantStrategyResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) QuantStyle style,
            @RequestParam(required = false) StrategyStatus status,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String range,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int perPage,
            @RequestParam(required = false) String sort
    ) {
        JsonNode filterNode = FilterParamParser.parse(filter);
        String resolvedKeyword = keyword != null ? keyword : FilterParamParser.text(filterNode, "keyword");
        QuantStyle resolvedStyle = style != null ? style : FilterParamParser.enumValue(filterNode, "style", QuantStyle.class);
        StrategyStatus resolvedStatus = status != null
                ? status
                : FilterParamParser.enumValue(filterNode, "status", StrategyStatus.class);

        Pageable pageable = PageableFactory.quantStrategyPageable(page, perPage, sort, range);
        Page<QuantStrategyResponse> result = quantStrategyService.search(
                        resolvedKeyword,
                        resolvedStyle,
                        resolvedStatus,
                        pageable
                )
                .map(QuantStrategyMapper::toDto);

        List<QuantStrategyResponse> data = result.getContent();
        return ResponseEntity.ok(new ApiListResponse<>(data, result.getTotalElements()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiSingleResponse<QuantStrategyResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody QuantStrategyUpdateRequest request
    ) {
        QuantStrategyResponse response = QuantStrategyMapper.toDto(quantStrategyService.update(id, request));
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        quantStrategyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
