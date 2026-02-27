package com.quant.portal.api.application.service;

import com.quant.portal.api.application.exception.ApiException;
import com.quant.portal.api.application.exception.ErrorCode;
import com.quant.portal.api.application.mapper.QuantStrategyMapper;
import com.quant.portal.api.application.query.QuantStrategySearchCondition;
import com.quant.portal.api.infrastructure.jpa.repository.QuantSignalRepository;
import com.quant.portal.api.infrastructure.jpa.repository.QuantStrategyRepository;
import com.quant.portal.api.presentation.dto.quantstrategy.QuantStrategyCreateRequest;
import com.quant.portal.api.presentation.dto.quantstrategy.QuantStrategyUpdateRequest;
import com.quant.portal.domain.quant.entity.QuantStrategy;
import com.quant.portal.domain.quant.enums.QuantStyle;
import com.quant.portal.domain.quant.enums.StrategyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuantStrategyService {

    private final QuantStrategyRepository quantStrategyRepository;
    private final QuantSignalRepository quantSignalRepository;

    public QuantStrategyService(
            QuantStrategyRepository quantStrategyRepository,
            QuantSignalRepository quantSignalRepository
    ) {
        this.quantStrategyRepository = quantStrategyRepository;
        this.quantSignalRepository = quantSignalRepository;
    }

    @Transactional
    public QuantStrategy create(QuantStrategyCreateRequest request) {
        QuantStrategy strategy = QuantStrategyMapper.toEntity(request);
        return quantStrategyRepository.save(strategy);
    }

    @Transactional(readOnly = true)
    public QuantStrategy get(Long id) {
        return quantStrategyRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Quant strategy not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<QuantStrategy> search(String keyword, QuantStyle style, StrategyStatus status, Pageable pageable) {
        QuantStrategySearchCondition condition = new QuantStrategySearchCondition(keyword, style, status);
        return quantStrategyRepository.search(condition, pageable);
    }

    @Transactional
    public QuantStrategy update(Long id, QuantStrategyUpdateRequest request) {
        QuantStrategy strategy = get(id);
        strategy.update(
                request.name(),
                request.style(),
                request.status(),
                request.rebalanceCycleDays(),
                request.description()
        );
        return strategy;
    }

    @Transactional
    public void delete(Long id) {
        QuantStrategy strategy = get(id);

        long signalCount = quantSignalRepository.countByStrategyId(id);
        if (signalCount > 0) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    ErrorCode.CONFLICT,
                    "Quant strategy cannot be deleted because signals exist",
                    "signalCount=" + signalCount
            );
        }

        quantStrategyRepository.delete(strategy);
    }
}
