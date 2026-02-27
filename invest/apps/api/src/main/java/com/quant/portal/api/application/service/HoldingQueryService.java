package com.quant.portal.api.application.service;

import com.quant.portal.api.application.exception.ApiException;
import com.quant.portal.api.application.exception.ErrorCode;
import com.quant.portal.api.application.query.HoldingSearchCondition;
import com.quant.portal.api.infrastructure.jpa.repository.HoldingRepository;
import com.quant.portal.domain.portfolio.entity.Holding;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HoldingQueryService {

    private final HoldingRepository holdingRepository;

    public HoldingQueryService(HoldingRepository holdingRepository) {
        this.holdingRepository = holdingRepository;
    }

    @Transactional(readOnly = true)
    public Holding get(Long id) {
        return holdingRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Holding not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Holding> findByCondition(Long portfolioId, Long instrumentId, String keyword, Pageable pageable) {
        HoldingSearchCondition condition = new HoldingSearchCondition(portfolioId, instrumentId, keyword);
        return holdingRepository.search(condition, pageable);
    }
}
