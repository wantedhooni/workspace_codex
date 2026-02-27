package com.quant.portal.api.presentation.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.quant.portal.api.application.mapper.TransactionMapper;
import com.quant.portal.api.application.query.PageableFactory;
import com.quant.portal.api.application.service.TransactionCommandService;
import com.quant.portal.api.application.service.TransactionQueryService;
import com.quant.portal.api.presentation.dto.ApiListResponse;
import com.quant.portal.api.presentation.dto.ApiSingleResponse;
import com.quant.portal.api.presentation.filter.FilterParamParser;
import com.quant.portal.api.presentation.dto.transaction.TransactionCreateRequest;
import com.quant.portal.api.presentation.dto.transaction.TransactionResponse;
import com.quant.portal.api.presentation.dto.transaction.TransactionUpdateRequest;
import com.quant.portal.domain.portfolio.enums.TransactionType;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionCommandService transactionCommandService;
    private final TransactionQueryService transactionQueryService;

    public TransactionController(
            TransactionCommandService transactionCommandService,
            TransactionQueryService transactionQueryService
    ) {
        this.transactionCommandService = transactionCommandService;
        this.transactionQueryService = transactionQueryService;
    }

    @PostMapping
    public ResponseEntity<ApiSingleResponse<TransactionResponse>> create(@Valid @RequestBody TransactionCreateRequest request) {
        TransactionResponse response = TransactionMapper.toDto(
                transactionCommandService.register(TransactionMapper.toCommand(request))
        );
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiSingleResponse<TransactionResponse>> get(@PathVariable Long id) {
        TransactionResponse response = TransactionMapper.toDto(transactionQueryService.get(id));
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiSingleResponse<TransactionResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody TransactionUpdateRequest request
    ) {
        TransactionResponse response = TransactionMapper.toDto(transactionCommandService.update(id, request));
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }

    @GetMapping
    public ResponseEntity<ApiListResponse<TransactionResponse>> list(
            @RequestParam(required = false) Long portfolioId,
            @RequestParam(required = false) Long instrumentId,
            @RequestParam(required = false) TransactionType transactionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String range,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int perPage,
            @RequestParam(required = false) String sort
    ) {
        JsonNode filterNode = FilterParamParser.parse(filter);
        Long resolvedPortfolioId = portfolioId != null ? portfolioId : FilterParamParser.longValue(filterNode, "portfolioId");

        Long resolvedInstrumentId = instrumentId != null ? instrumentId : FilterParamParser.longValue(filterNode, "instrumentId");
        TransactionType resolvedTransactionType = transactionType != null ? transactionType
                : FilterParamParser.enumValue(filterNode, "transactionType", TransactionType.class);
        LocalDate resolvedFromDate = fromDate != null ? fromDate : FilterParamParser.localDate(filterNode, "fromDate");
        LocalDate resolvedToDate = toDate != null ? toDate : FilterParamParser.localDate(filterNode, "toDate");

        Pageable pageable = PageableFactory.transactionPageable(page, perPage, sort, range);
        Page<TransactionResponse> result = transactionQueryService.findByCondition(
                        resolvedPortfolioId,
                        resolvedInstrumentId,
                        resolvedTransactionType,
                        resolvedFromDate,
                        resolvedToDate,
                        pageable
                )
                .map(TransactionMapper::toDto);

        List<TransactionResponse> data = result.getContent();
        return ResponseEntity.ok(new ApiListResponse<>(data, result.getTotalElements()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transactionCommandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
