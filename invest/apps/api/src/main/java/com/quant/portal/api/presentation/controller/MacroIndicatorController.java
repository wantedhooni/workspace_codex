package com.quant.portal.api.presentation.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.quant.portal.api.application.mapper.MacroIndicatorMapper;
import com.quant.portal.api.application.query.PageableFactory;
import com.quant.portal.api.application.service.MacroIndicatorService;
import com.quant.portal.api.presentation.dto.ApiListResponse;
import com.quant.portal.api.presentation.dto.ApiSingleResponse;
import com.quant.portal.api.presentation.dto.macroindicator.MacroIndicatorCreateRequest;
import com.quant.portal.api.presentation.dto.macroindicator.MacroIndicatorResponse;
import com.quant.portal.api.presentation.dto.macroindicator.MacroIndicatorUpdateRequest;
import com.quant.portal.api.presentation.filter.FilterParamParser;
import com.quant.portal.domain.macro.enums.MacroRegionCode;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/api/v1/macro-indicators")
public class MacroIndicatorController {

    private final MacroIndicatorService macroIndicatorService;

    public MacroIndicatorController(MacroIndicatorService macroIndicatorService) {
        this.macroIndicatorService = macroIndicatorService;
    }

    @PostMapping
    public ResponseEntity<ApiSingleResponse<MacroIndicatorResponse>> create(
            @Valid @RequestBody MacroIndicatorCreateRequest request
    ) {
        MacroIndicatorResponse response = MacroIndicatorMapper.toDto(macroIndicatorService.create(request));
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiSingleResponse<MacroIndicatorResponse>> get(@PathVariable Long id) {
        MacroIndicatorResponse response = MacroIndicatorMapper.toDto(macroIndicatorService.get(id));
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }

    @GetMapping
    public ResponseEntity<ApiListResponse<MacroIndicatorResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) MacroRegionCode regionCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String range,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int perPage,
            @RequestParam(required = false) String sort
    ) {
        JsonNode filterNode = FilterParamParser.parse(filter);
        String resolvedKeyword = keyword != null ? keyword : FilterParamParser.text(filterNode, "keyword");
        MacroRegionCode resolvedRegionCode = regionCode != null
                ? regionCode
                : FilterParamParser.enumValue(filterNode, "regionCode", MacroRegionCode.class);
        LocalDate resolvedFromDate = fromDate != null ? fromDate : FilterParamParser.localDate(filterNode, "fromDate");
        LocalDate resolvedToDate = toDate != null ? toDate : FilterParamParser.localDate(filterNode, "toDate");

        Pageable pageable = PageableFactory.macroIndicatorPageable(page, perPage, sort, range);
        Page<MacroIndicatorResponse> result = macroIndicatorService.search(
                        resolvedKeyword,
                        resolvedRegionCode,
                        resolvedFromDate,
                        resolvedToDate,
                        pageable
                )
                .map(MacroIndicatorMapper::toDto);

        List<MacroIndicatorResponse> data = result.getContent();
        return ResponseEntity.ok(new ApiListResponse<>(data, result.getTotalElements()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiSingleResponse<MacroIndicatorResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody MacroIndicatorUpdateRequest request
    ) {
        MacroIndicatorResponse response = MacroIndicatorMapper.toDto(macroIndicatorService.update(id, request));
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        macroIndicatorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
