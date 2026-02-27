package com.quant.portal.api.presentation.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.quant.portal.api.application.mapper.QuantSignalMapper;
import com.quant.portal.api.application.mapper.TransactionMapper;
import com.quant.portal.api.application.query.PageableFactory;
import com.quant.portal.api.application.service.QuantSignalService;
import com.quant.portal.api.presentation.dto.ApiListResponse;
import com.quant.portal.api.presentation.dto.ApiSingleResponse;
import com.quant.portal.api.presentation.dto.quantsignal.QuantSignalCreateRequest;
import com.quant.portal.api.presentation.dto.quantsignal.QuantSignalExecutionResponse;
import com.quant.portal.api.presentation.dto.quantsignal.QuantSignalExecuteRequest;
import com.quant.portal.api.presentation.dto.quantsignal.QuantSignalResponse;
import com.quant.portal.api.presentation.dto.quantsignal.QuantSignalUpdateRequest;
import com.quant.portal.api.presentation.dto.transaction.TransactionResponse;
import com.quant.portal.api.presentation.filter.FilterParamParser;
import com.quant.portal.domain.quant.enums.SignalType;
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
@RequestMapping("/api/v1/quant-signals")
public class QuantSignalController {

    private final QuantSignalService quantSignalService;

    public QuantSignalController(QuantSignalService quantSignalService) {
        this.quantSignalService = quantSignalService;
    }

    @PostMapping
    public ResponseEntity<ApiSingleResponse<QuantSignalResponse>> create(@Valid @RequestBody QuantSignalCreateRequest request) {
        QuantSignalResponse response = QuantSignalMapper.toDto(quantSignalService.create(request));
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiSingleResponse<QuantSignalResponse>> get(@PathVariable Long id) {
        QuantSignalResponse response = QuantSignalMapper.toDto(quantSignalService.get(id));
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }

    @GetMapping("/{id}/execution")
    public ResponseEntity<ApiSingleResponse<QuantSignalExecutionResponse>> getExecution(@PathVariable Long id) {
        QuantSignalExecutionResponse response = QuantSignalMapper.toExecutionDto(quantSignalService.getExecution(id));
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }

    @GetMapping
    public ResponseEntity<ApiListResponse<QuantSignalResponse>> list(
            @RequestParam(required = false) Long strategyId,
            @RequestParam(required = false) Long instrumentId,
            @RequestParam(required = false) SignalType signalType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String range,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int perPage,
            @RequestParam(required = false) String sort
    ) {
        JsonNode filterNode = FilterParamParser.parse(filter);
        Long resolvedStrategyId = strategyId != null ? strategyId : FilterParamParser.longValue(filterNode, "strategyId");
        Long resolvedInstrumentId = instrumentId != null ? instrumentId : FilterParamParser.longValue(filterNode, "instrumentId");
        SignalType resolvedSignalType = signalType != null
                ? signalType
                : FilterParamParser.enumValue(filterNode, "signalType", SignalType.class);
        LocalDate resolvedFromDate = fromDate != null ? fromDate : FilterParamParser.localDate(filterNode, "fromDate");
        LocalDate resolvedToDate = toDate != null ? toDate : FilterParamParser.localDate(filterNode, "toDate");

        Pageable pageable = PageableFactory.quantSignalPageable(page, perPage, sort, range);
        Page<QuantSignalResponse> result = quantSignalService.search(
                        resolvedStrategyId,
                        resolvedInstrumentId,
                        resolvedSignalType,
                        resolvedFromDate,
                        resolvedToDate,
                        pageable
                )
                .map(QuantSignalMapper::toDto);

        List<QuantSignalResponse> data = result.getContent();
        return ResponseEntity.ok(new ApiListResponse<>(data, result.getTotalElements()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiSingleResponse<QuantSignalResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody QuantSignalUpdateRequest request
    ) {
        QuantSignalResponse response = QuantSignalMapper.toDto(quantSignalService.update(id, request));
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<ApiSingleResponse<TransactionResponse>> execute(
            @PathVariable Long id,
            @Valid @RequestBody QuantSignalExecuteRequest request
    ) {
        TransactionResponse response = TransactionMapper.toDto(quantSignalService.execute(id, request));
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        quantSignalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
