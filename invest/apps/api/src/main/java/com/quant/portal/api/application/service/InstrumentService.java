package com.quant.portal.api.application.service;

import com.quant.portal.api.application.exception.ApiException;
import com.quant.portal.api.application.exception.ErrorCode;
import com.quant.portal.api.application.mapper.InstrumentMapper;
import com.quant.portal.api.application.query.InstrumentSearchCondition;
import com.quant.portal.api.infrastructure.jpa.repository.HoldingRepository;
import com.quant.portal.api.infrastructure.jpa.repository.InstrumentRepository;
import com.quant.portal.api.infrastructure.jpa.repository.PortfolioTransactionRepository;
import com.quant.portal.api.presentation.dto.instrument.InstrumentCreateRequest;
import com.quant.portal.api.presentation.dto.instrument.InstrumentUpdateRequest;
import com.quant.portal.domain.portfolio.entity.Instrument;
import com.quant.portal.domain.portfolio.enums.CurrencyCode;
import com.quant.portal.domain.portfolio.enums.MarketCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InstrumentService {

    private final InstrumentRepository instrumentRepository;
    private final HoldingRepository holdingRepository;
    private final PortfolioTransactionRepository portfolioTransactionRepository;

    public InstrumentService(
            InstrumentRepository instrumentRepository,
            HoldingRepository holdingRepository,
            PortfolioTransactionRepository portfolioTransactionRepository
    ) {
        this.instrumentRepository = instrumentRepository;
        this.holdingRepository = holdingRepository;
        this.portfolioTransactionRepository = portfolioTransactionRepository;
    }

    @Transactional
    public Instrument create(InstrumentCreateRequest request) {
        instrumentRepository.findByMarketCodeAndTicker(request.marketCode(), request.ticker())
                .ifPresent(instrument -> {
                    throw new ApiException(HttpStatus.CONFLICT, ErrorCode.CONFLICT, "Instrument already exists");
                });

        return instrumentRepository.save(InstrumentMapper.toEntity(request));
    }

    @Transactional(readOnly = true)
    public Instrument get(Long id) {
        return instrumentRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Instrument not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Instrument> search(String keyword, MarketCode marketCode, CurrencyCode currencyCode, Pageable pageable) {
        InstrumentSearchCondition condition = new InstrumentSearchCondition(keyword, marketCode, currencyCode);
        return instrumentRepository.search(condition, pageable);
    }

    @Transactional
    public Instrument update(Long id, InstrumentUpdateRequest request) {
        Instrument instrument = get(id);
        instrument.rename(request.name());
        return instrument;
    }

    @Transactional
    public void delete(Long id) {
        Instrument instrument = get(id);

        long transactionCount = portfolioTransactionRepository.countByInstrumentId(id);
        if (transactionCount > 0) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    ErrorCode.CONFLICT,
                    "Instrument cannot be deleted because transactions exist",
                    "transactionCount=" + transactionCount
            );
        }

        long holdingCount = holdingRepository.countByInstrumentId(id);
        if (holdingCount > 0) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    ErrorCode.CONFLICT,
                    "Instrument cannot be deleted because holdings exist",
                    "holdingCount=" + holdingCount
            );
        }

        instrumentRepository.delete(instrument);
    }
}
