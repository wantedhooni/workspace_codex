package com.quant.portal.api.application.service;

import com.quant.portal.api.application.exception.ApiException;
import com.quant.portal.api.application.exception.ErrorCode;
import com.quant.portal.api.application.mapper.QuantSignalMapper;
import com.quant.portal.api.application.query.QuantSignalSearchCondition;
import com.quant.portal.api.application.service.command.RegisterTransactionCommand;
import com.quant.portal.api.infrastructure.jpa.repository.InstrumentRepository;
import com.quant.portal.api.infrastructure.jpa.repository.QuantSignalExecutionRepository;
import com.quant.portal.api.infrastructure.jpa.repository.QuantSignalRepository;
import com.quant.portal.api.infrastructure.jpa.repository.QuantStrategyRepository;
import com.quant.portal.api.presentation.dto.quantsignal.QuantSignalCreateRequest;
import com.quant.portal.api.presentation.dto.quantsignal.QuantSignalExecuteRequest;
import com.quant.portal.api.presentation.dto.quantsignal.QuantSignalUpdateRequest;
import com.quant.portal.domain.portfolio.entity.Instrument;
import com.quant.portal.domain.portfolio.entity.PortfolioTransaction;
import com.quant.portal.domain.portfolio.enums.TransactionType;
import com.quant.portal.domain.quant.entity.QuantSignal;
import com.quant.portal.domain.quant.entity.QuantSignalExecution;
import com.quant.portal.domain.quant.entity.QuantStrategy;
import com.quant.portal.domain.quant.enums.SignalType;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuantSignalService {

    private final QuantSignalRepository quantSignalRepository;
    private final QuantSignalExecutionRepository quantSignalExecutionRepository;
    private final QuantStrategyRepository quantStrategyRepository;
    private final InstrumentRepository instrumentRepository;
    private final TransactionCommandService transactionCommandService;

    public QuantSignalService(
            QuantSignalRepository quantSignalRepository,
            QuantSignalExecutionRepository quantSignalExecutionRepository,
            QuantStrategyRepository quantStrategyRepository,
            InstrumentRepository instrumentRepository,
            TransactionCommandService transactionCommandService
    ) {
        this.quantSignalRepository = quantSignalRepository;
        this.quantSignalExecutionRepository = quantSignalExecutionRepository;
        this.quantStrategyRepository = quantStrategyRepository;
        this.instrumentRepository = instrumentRepository;
        this.transactionCommandService = transactionCommandService;
    }

    @Transactional
    public QuantSignal create(QuantSignalCreateRequest request) {
        QuantStrategy strategy = getStrategy(request.strategyId());
        Instrument instrument = getInstrument(request.instrumentId());

        QuantSignal signal = QuantSignalMapper.toEntity(request, strategy, instrument);
        return quantSignalRepository.save(signal);
    }

    @Transactional(readOnly = true)
    public QuantSignal get(Long id) {
        return quantSignalRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Quant signal not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<QuantSignal> search(
            Long strategyId,
            Long instrumentId,
            SignalType signalType,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    ) {
        QuantSignalSearchCondition condition = new QuantSignalSearchCondition(
                strategyId,
                instrumentId,
                signalType,
                fromDate,
                toDate
        );
        return quantSignalRepository.search(condition, pageable);
    }

    @Transactional
    public QuantSignal update(Long id, QuantSignalUpdateRequest request) {
        QuantSignal signal = get(id);
        Instrument instrument = getInstrument(request.instrumentId());
        signal.update(
                instrument,
                request.signalType(),
                request.signalDate(),
                request.score(),
                request.confidence(),
                request.rationale()
        );
        return signal;
    }

    @Transactional
    public void delete(Long id) {
        QuantSignal signal = get(id);
        quantSignalRepository.delete(signal);
    }

    @Transactional
    public PortfolioTransaction execute(Long id, QuantSignalExecuteRequest request) {
        QuantSignal signal = get(id);
        if (quantSignalExecutionRepository.existsBySignal_Id(signal.getId())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    ErrorCode.CONFLICT,
                    "Quant signal already executed: " + id,
                    Map.of("signalId", "Signal can only be executed once")
            );
        }

        TransactionType transactionType = resolveTransactionType(signal.getSignalType());
        RegisterTransactionCommand command = new RegisterTransactionCommand(
                request.portfolioId(),
                signal.getInstrument().getId(),
                transactionType,
                request.tradeDate() == null ? signal.getSignalDate() : request.tradeDate(),
                request.quantity(),
                request.unitPrice(),
                null,
                request.fee(),
                request.tax(),
                request.currencyCode(),
                composeExecutionMemo(signal, request.memo())
        );
        PortfolioTransaction transaction = transactionCommandService.register(command);
        quantSignalExecutionRepository.save(new QuantSignalExecution(signal, transaction));
        return transaction;
    }

    @Transactional(readOnly = true)
    public QuantSignalExecution getExecution(Long id) {
        QuantSignal signal = get(id);
        return quantSignalExecutionRepository.findBySignal_Id(signal.getId()).orElse(null);
    }

    private QuantStrategy getStrategy(Long id) {
        return quantStrategyRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Quant strategy not found: " + id));
    }

    private Instrument getInstrument(Long id) {
        return instrumentRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Instrument not found: " + id));
    }

    private static TransactionType resolveTransactionType(SignalType signalType) {
        if (signalType == SignalType.BUY) {
            return TransactionType.BUY;
        }
        if (signalType == SignalType.SELL) {
            return TransactionType.SELL;
        }
        throw new ApiException(
                HttpStatus.BAD_REQUEST,
                ErrorCode.BAD_REQUEST,
                "Signal type %s cannot be executed as transaction".formatted(signalType),
                Map.of("signalType", "Only BUY and SELL signals can be executed")
        );
    }

    private static String composeExecutionMemo(QuantSignal signal, String memo) {
        String prefix = "signal#" + signal.getId() + " " + signal.getSignalType();
        if (memo == null || memo.isBlank()) {
            return prefix;
        }
        return prefix + " - " + memo.trim();
    }
}
