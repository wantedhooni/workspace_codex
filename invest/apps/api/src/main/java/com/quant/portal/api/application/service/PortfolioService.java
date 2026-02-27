package com.quant.portal.api.application.service;

import com.quant.portal.api.application.exception.ApiException;
import com.quant.portal.api.application.exception.ErrorCode;
import com.quant.portal.api.application.mapper.PortfolioMapper;
import com.quant.portal.api.application.query.PortfolioSearchCondition;
import com.quant.portal.api.infrastructure.jpa.repository.HoldingRepository;
import com.quant.portal.api.infrastructure.jpa.repository.PortfolioRepository;
import com.quant.portal.api.infrastructure.jpa.repository.PortfolioTransactionRepository;
import com.quant.portal.api.presentation.dto.portfolio.PortfolioCreateRequest;
import com.quant.portal.api.presentation.dto.portfolio.PortfolioUpdateRequest;
import com.quant.portal.domain.portfolio.entity.Portfolio;
import com.quant.portal.domain.portfolio.enums.CurrencyCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final HoldingRepository holdingRepository;
    private final PortfolioTransactionRepository portfolioTransactionRepository;

    public PortfolioService(
            PortfolioRepository portfolioRepository,
            HoldingRepository holdingRepository,
            PortfolioTransactionRepository portfolioTransactionRepository
    ) {
        this.portfolioRepository = portfolioRepository;
        this.holdingRepository = holdingRepository;
        this.portfolioTransactionRepository = portfolioTransactionRepository;
    }

    @Transactional
    public Portfolio create(PortfolioCreateRequest request) {
        Portfolio portfolio = PortfolioMapper.toEntity(request);
        return portfolioRepository.save(portfolio);
    }

    @Transactional(readOnly = true)
    public Portfolio get(Long id) {
        return portfolioRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Portfolio not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Portfolio> search(String keyword, CurrencyCode baseCurrency, Pageable pageable) {
        PortfolioSearchCondition condition = new PortfolioSearchCondition(keyword, baseCurrency);
        return portfolioRepository.search(condition, pageable);
    }

    @Transactional
    public Portfolio update(Long id, PortfolioUpdateRequest request) {
        Portfolio portfolio = get(id);
        portfolio.rename(request.name());
        return portfolio;
    }

    @Transactional
    public void delete(Long id) {
        Portfolio portfolio = get(id);

        long transactionCount = portfolioTransactionRepository.countByPortfolioId(id);
        if (transactionCount > 0) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    ErrorCode.CONFLICT,
                    "Portfolio cannot be deleted because transactions exist",
                    "transactionCount=" + transactionCount
            );
        }

        long holdingCount = holdingRepository.countByPortfolioId(id);
        if (holdingCount > 0) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    ErrorCode.CONFLICT,
                    "Portfolio cannot be deleted because holdings exist",
                    "holdingCount=" + holdingCount
            );
        }

        portfolioRepository.delete(portfolio);
    }
}
