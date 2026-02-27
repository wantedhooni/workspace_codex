package com.quant.portal.api.presentation.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.quant.portal.api.application.mapper.HoldingMapper;
import com.quant.portal.api.application.query.PageableFactory;
import com.quant.portal.api.application.service.HoldingQueryService;
import com.quant.portal.api.presentation.dto.ApiListResponse;
import com.quant.portal.api.presentation.dto.ApiSingleResponse;
import com.quant.portal.api.presentation.filter.FilterParamParser;
import com.quant.portal.api.presentation.dto.holding.HoldingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/holdings")
public class HoldingController {

    private final HoldingQueryService holdingQueryService;

    public HoldingController(HoldingQueryService holdingQueryService) {
        this.holdingQueryService = holdingQueryService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiSingleResponse<HoldingResponse>> get(@PathVariable Long id) {
        HoldingResponse response = HoldingMapper.toDto(holdingQueryService.get(id));
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }

    @GetMapping
    public ResponseEntity<ApiListResponse<HoldingResponse>> list(
            @RequestParam(required = false) Long portfolioId,
            @RequestParam(required = false) Long instrumentId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String range,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int perPage,
            @RequestParam(required = false) String sort
    ) {
        JsonNode filterNode = FilterParamParser.parse(filter);
        Long resolvedPortfolioId = portfolioId != null ? portfolioId : FilterParamParser.longValue(filterNode, "portfolioId");
        Long resolvedInstrumentId = instrumentId != null ? instrumentId : FilterParamParser.longValue(filterNode, "instrumentId");
        String resolvedKeyword = keyword != null ? keyword : FilterParamParser.text(filterNode, "keyword");

        Pageable pageable = PageableFactory.holdingPageable(page, perPage, sort, range);
        Page<HoldingResponse> result = holdingQueryService.findByCondition(
                        resolvedPortfolioId,
                        resolvedInstrumentId,
                        resolvedKeyword,
                        pageable
                )
                .map(HoldingMapper::toDto);
        List<HoldingResponse> data = result.getContent();
        return ResponseEntity.ok(new ApiListResponse<>(data, result.getTotalElements()));
    }
}
