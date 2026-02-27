package com.quant.portal.api.application.service;

import com.quant.portal.api.application.exception.ApiException;
import com.quant.portal.api.application.exception.ErrorCode;
import com.quant.portal.api.application.query.TransactionSearchCondition;
import com.quant.portal.api.infrastructure.jpa.repository.PortfolioTransactionRepository;
import com.quant.portal.domain.portfolio.entity.PortfolioTransaction;
import com.quant.portal.domain.portfolio.enums.TransactionType;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionQueryService {

    private final PortfolioTransactionRepository portfolioTransactionRepository;

    public TransactionQueryService(PortfolioTransactionRepository portfolioTransactionRepository) {
        this.portfolioTransactionRepository = portfolioTransactionRepository;
    }

    @Transactional(readOnly = true)
    public PortfolioTransaction get(Long id) {
        return portfolioTransactionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Transaction not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<PortfolioTransaction> findByCondition(
            Long portfolioId,
            Long instrumentId,
            TransactionType transactionType,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    ) {
        TransactionSearchCondition condition = new TransactionSearchCondition(
                portfolioId,
                instrumentId,
                transactionType,
                fromDate,
                toDate
        );
        return portfolioTransactionRepository.search(condition, pageable);
    }
}
