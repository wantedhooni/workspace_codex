package com.quant.portal.api.application.service;

import com.quant.portal.api.application.exception.ApiException;
import com.quant.portal.api.application.exception.ErrorCode;
import com.quant.portal.api.application.mapper.MacroIndicatorMapper;
import com.quant.portal.api.application.query.MacroIndicatorSearchCondition;
import com.quant.portal.api.infrastructure.jpa.repository.MacroIndicatorRepository;
import com.quant.portal.api.presentation.dto.macroindicator.MacroIndicatorCreateRequest;
import com.quant.portal.api.presentation.dto.macroindicator.MacroIndicatorUpdateRequest;
import com.quant.portal.domain.macro.entity.MacroIndicator;
import com.quant.portal.domain.macro.enums.MacroRegionCode;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MacroIndicatorService {

    private final MacroIndicatorRepository macroIndicatorRepository;

    public MacroIndicatorService(MacroIndicatorRepository macroIndicatorRepository) {
        this.macroIndicatorRepository = macroIndicatorRepository;
    }

    @Transactional
    public MacroIndicator create(MacroIndicatorCreateRequest request) {
        macroIndicatorRepository.findByIndicatorCodeAndRegionCodeAndObservedDate(
                        request.indicatorCode(),
                        request.regionCode(),
                        request.observedDate()
                )
                .ifPresent(indicator -> {
                    throw new ApiException(HttpStatus.CONFLICT, ErrorCode.CONFLICT, "Macro indicator already exists");
                });

        MacroIndicator indicator = MacroIndicatorMapper.toEntity(request);
        return macroIndicatorRepository.save(indicator);
    }

    @Transactional(readOnly = true)
    public MacroIndicator get(Long id) {
        return macroIndicatorRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Macro indicator not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<MacroIndicator> search(
            String keyword,
            MacroRegionCode regionCode,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    ) {
        MacroIndicatorSearchCondition condition = new MacroIndicatorSearchCondition(keyword, regionCode, fromDate, toDate);
        return macroIndicatorRepository.search(condition, pageable);
    }

    @Transactional
    public MacroIndicator update(Long id, MacroIndicatorUpdateRequest request) {
        MacroIndicator indicator = get(id);
        indicator.update(
                request.indicatorName(),
                request.regionCode(),
                request.observedDate(),
                request.indicatorValue(),
                request.unit(),
                request.source()
        );
        return indicator;
    }

    @Transactional
    public void delete(Long id) {
        MacroIndicator indicator = get(id);
        macroIndicatorRepository.delete(indicator);
    }
}
