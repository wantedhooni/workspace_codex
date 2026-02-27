package com.quant.portal.api.application.service;

import com.quant.portal.api.application.exception.ApiException;
import com.quant.portal.api.application.exception.ErrorCode;
import com.quant.portal.api.application.service.command.RegisterTransactionCommand;
import com.quant.portal.api.infrastructure.jpa.repository.HoldingRepository;
import com.quant.portal.api.infrastructure.jpa.repository.InstrumentRepository;
import com.quant.portal.api.infrastructure.jpa.repository.PortfolioRepository;
import com.quant.portal.api.infrastructure.jpa.repository.PortfolioTransactionRepository;
import com.quant.portal.api.presentation.dto.transaction.TransactionUpdateRequest;
import com.quant.portal.domain.portfolio.entity.Holding;
import com.quant.portal.domain.portfolio.entity.Instrument;
import com.quant.portal.domain.portfolio.entity.Portfolio;
import com.quant.portal.domain.portfolio.entity.PortfolioTransaction;
import com.quant.portal.domain.portfolio.enums.CurrencyCode;
import com.quant.portal.domain.portfolio.enums.TransactionType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionCommandService {

    private static final int MONEY_SCALE = 4;

    private final PortfolioRepository portfolioRepository;
    private final InstrumentRepository instrumentRepository;
    private final HoldingRepository holdingRepository;
    private final PortfolioTransactionRepository portfolioTransactionRepository;

    public TransactionCommandService(
            PortfolioRepository portfolioRepository,
            InstrumentRepository instrumentRepository,
            HoldingRepository holdingRepository,
            PortfolioTransactionRepository portfolioTransactionRepository
    ) {
        this.portfolioRepository = portfolioRepository;
        this.instrumentRepository = instrumentRepository;
        this.holdingRepository = holdingRepository;
        this.portfolioTransactionRepository = portfolioTransactionRepository;
    }

    @Transactional
    public PortfolioTransaction register(RegisterTransactionCommand command) {
        Portfolio portfolio = portfolioRepository.findById(command.portfolioId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Portfolio not found: " + command.portfolioId()));

        BigDecimal fee = requireNonNegative(defaultZero(command.fee()), "fee");
        BigDecimal tax = requireNonNegative(defaultZero(command.tax()), "tax");
        CurrencyCode currencyCode = command.currencyCode() == null ? portfolio.getBaseCurrency() : command.currencyCode();

        Instrument instrument = loadInstrumentIfNeeded(command);

        BigDecimal amount;
        BigDecimal cashImpact;
        BigDecimal realizedPnl = null;

        try {
            switch (command.transactionType()) {
                case BUY -> {
                    ensureNull(command.amount(), "amount", command.transactionType());
                    BigDecimal quantity = requirePositive(command.quantity(), "quantity");
                    BigDecimal unitPrice = requirePositive(command.unitPrice(), "unitPrice");
                    ensureInstrumentExists(command, instrument);

                    BigDecimal grossCost = normalizeMoney(unitPrice.multiply(quantity));
                    amount = normalizeMoney(grossCost.add(fee).add(tax));
                    cashImpact = amount.negate();

                    portfolio.applyCashDelta(cashImpact);

                    Holding holding = holdingRepository.findByPortfolioIdAndInstrumentId(portfolio.getId(), instrument.getId())
                            .orElseGet(() -> new Holding(portfolio, instrument));
                    holding.applyBuy(quantity, amount);
                    holdingRepository.save(holding);
                }
                case SELL -> {
                    ensureNull(command.amount(), "amount", command.transactionType());
                    BigDecimal quantity = requirePositive(command.quantity(), "quantity");
                    BigDecimal unitPrice = requirePositive(command.unitPrice(), "unitPrice");
                    ensureInstrumentExists(command, instrument);

                    Holding holding = holdingRepository.findByPortfolioIdAndInstrumentId(portfolio.getId(), instrument.getId())
                            .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "No holding found for sell transaction"));

                    BigDecimal proceeds = normalizeMoney(unitPrice.multiply(quantity).subtract(fee).subtract(tax));
                    BigDecimal costBasis = normalizeMoney(holding.getAverageCost().multiply(quantity));
                    realizedPnl = normalizeMoney(proceeds.subtract(costBasis));

                    holding.applySell(quantity);
                    if (holding.isEmpty()) {
                        holdingRepository.delete(holding);
                    } else {
                        holdingRepository.save(holding);
                    }

                    amount = proceeds;
                    cashImpact = proceeds;
                    portfolio.applyCashDelta(cashImpact);
                }
                case DEPOSIT -> {
                    ensureNull(command.instrumentId(), "instrumentId", command.transactionType());
                    ensureNull(command.quantity(), "quantity", command.transactionType());
                    ensureNull(command.unitPrice(), "unitPrice", command.transactionType());
                    amount = requirePositive(command.amount(), "amount");
                    cashImpact = amount;
                    portfolio.applyCashDelta(cashImpact);
                }
                case WITHDRAW -> {
                    ensureNull(command.instrumentId(), "instrumentId", command.transactionType());
                    ensureNull(command.quantity(), "quantity", command.transactionType());
                    ensureNull(command.unitPrice(), "unitPrice", command.transactionType());
                    amount = requirePositive(command.amount(), "amount");
                    cashImpact = amount.negate();
                    portfolio.applyCashDelta(cashImpact);
                }
                case DIVIDEND -> {
                    ensureNull(command.instrumentId(), "instrumentId", command.transactionType());
                    ensureNull(command.quantity(), "quantity", command.transactionType());
                    ensureNull(command.unitPrice(), "unitPrice", command.transactionType());
                    amount = requirePositive(command.amount(), "amount");
                    cashImpact = amount;
                    portfolio.applyCashDelta(cashImpact);
                }
                case FEE_ADJUST -> {
                    ensureNull(command.instrumentId(), "instrumentId", command.transactionType());
                    ensureNull(command.quantity(), "quantity", command.transactionType());
                    ensureNull(command.unitPrice(), "unitPrice", command.transactionType());
                    amount = requirePositive(command.amount(), "amount");
                    cashImpact = amount.negate();
                    portfolio.applyCashDelta(cashImpact);
                }
                default -> throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "Unsupported transaction type");
            }
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, exception.getMessage());
        }

        portfolioRepository.save(portfolio);

        PortfolioTransaction transaction = new PortfolioTransaction(
                portfolio,
                instrument,
                command.transactionType(),
                command.tradeDate(),
                command.quantity(),
                command.unitPrice(),
                normalizeMoney(amount),
                fee,
                tax,
                normalizeMoney(cashImpact),
                realizedPnl == null ? null : normalizeMoney(realizedPnl),
                currencyCode,
                command.memo()
        );

        return portfolioTransactionRepository.save(transaction);
    }

    @Transactional
    public PortfolioTransaction update(Long transactionId, TransactionUpdateRequest request) {
        PortfolioTransaction transaction = portfolioTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Transaction not found: " + transactionId));

        ensureLatestTransaction(transaction);

        Long portfolioId = transaction.getPortfolio().getId();
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Portfolio not found: " + portfolioId));

        rollbackOrThrow(portfolio, transaction);

        TransactionType transactionType = transaction.getTransactionType();
        Instrument instrument = requiresInstrument(transactionType) ? loadInstrumentForUpdate(request, transaction) : null;
        BigDecimal fee = requireNonNegative(defaultZero(request.fee()), "fee");
        BigDecimal tax = requireNonNegative(defaultZero(request.tax()), "tax");
        CurrencyCode currencyCode = request.currencyCode() == null ? portfolio.getBaseCurrency() : request.currencyCode();

        BigDecimal quantity = null;
        BigDecimal unitPrice = null;
        BigDecimal amount;
        BigDecimal cashImpact;
        BigDecimal realizedPnl = null;

        try {
            switch (transactionType) {
                case BUY -> {
                    ensureNull(request.amount(), "amount", transactionType);
                    quantity = requirePositive(request.quantity(), "quantity");
                    unitPrice = requirePositive(request.unitPrice(), "unitPrice");

                    BigDecimal grossCost = normalizeMoney(unitPrice.multiply(quantity));
                    amount = normalizeMoney(grossCost.add(fee).add(tax));
                    cashImpact = amount.negate();

                    portfolio.applyCashDelta(cashImpact);

                    Holding holding = holdingRepository.findByPortfolioIdAndInstrumentId(portfolioId, instrument.getId())
                            .orElseGet(() -> new Holding(portfolio, instrument));
                    holding.applyBuy(quantity, amount);
                    holdingRepository.save(holding);
                }
                case SELL -> {
                    ensureNull(request.amount(), "amount", transactionType);
                    quantity = requirePositive(request.quantity(), "quantity");
                    unitPrice = requirePositive(request.unitPrice(), "unitPrice");

                    Holding holding = holdingRepository.findByPortfolioIdAndInstrumentId(portfolioId, instrument.getId())
                            .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "No holding found for sell transaction"));

                    BigDecimal proceeds = normalizeMoney(unitPrice.multiply(quantity).subtract(fee).subtract(tax));
                    BigDecimal costBasis = normalizeMoney(holding.getAverageCost().multiply(quantity));
                    realizedPnl = normalizeMoney(proceeds.subtract(costBasis));

                    holding.applySell(quantity);
                    if (holding.isEmpty()) {
                        holdingRepository.delete(holding);
                    } else {
                        holdingRepository.save(holding);
                    }

                    amount = proceeds;
                    cashImpact = proceeds;
                    portfolio.applyCashDelta(cashImpact);
                }
                case DEPOSIT -> {
                    ensureNull(request.instrumentId(), "instrumentId", transactionType);
                    ensureNull(request.quantity(), "quantity", transactionType);
                    ensureNull(request.unitPrice(), "unitPrice", transactionType);
                    amount = requirePositive(request.amount(), "amount");
                    cashImpact = amount;
                    portfolio.applyCashDelta(cashImpact);
                }
                case WITHDRAW -> {
                    ensureNull(request.instrumentId(), "instrumentId", transactionType);
                    ensureNull(request.quantity(), "quantity", transactionType);
                    ensureNull(request.unitPrice(), "unitPrice", transactionType);
                    amount = requirePositive(request.amount(), "amount");
                    cashImpact = amount.negate();
                    portfolio.applyCashDelta(cashImpact);
                }
                case DIVIDEND -> {
                    ensureNull(request.instrumentId(), "instrumentId", transactionType);
                    ensureNull(request.quantity(), "quantity", transactionType);
                    ensureNull(request.unitPrice(), "unitPrice", transactionType);
                    amount = requirePositive(request.amount(), "amount");
                    cashImpact = amount;
                    portfolio.applyCashDelta(cashImpact);
                }
                case FEE_ADJUST -> {
                    ensureNull(request.instrumentId(), "instrumentId", transactionType);
                    ensureNull(request.quantity(), "quantity", transactionType);
                    ensureNull(request.unitPrice(), "unitPrice", transactionType);
                    amount = requirePositive(request.amount(), "amount");
                    cashImpact = amount.negate();
                    portfolio.applyCashDelta(cashImpact);
                }
                default -> throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "Unsupported transaction type");
            }
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, exception.getMessage());
        }

        transaction.update(
                instrument,
                request.tradeDate(),
                quantity,
                unitPrice,
                normalizeMoney(amount),
                fee,
                tax,
                normalizeMoney(cashImpact),
                realizedPnl == null ? null : normalizeMoney(realizedPnl),
                currencyCode,
                request.memo()
        );

        portfolioRepository.save(portfolio);
        return transaction;
    }

    @Transactional
    public void delete(Long transactionId) {
        PortfolioTransaction transaction = portfolioTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Transaction not found: " + transactionId));

        ensureLatestTransaction(transaction);

        Long portfolioId = transaction.getPortfolio().getId();
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Portfolio not found: " + portfolioId));

        rollbackOrThrow(portfolio, transaction);

        portfolioTransactionRepository.delete(transaction);
        portfolioRepository.save(portfolio);
    }

    private void ensureLatestTransaction(PortfolioTransaction transaction) {
        Long portfolioId = transaction.getPortfolio().getId();
        PortfolioTransaction latestTransaction = portfolioTransactionRepository.findTopByPortfolioIdOrderByTradeDateDescIdDesc(portfolioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Latest transaction not found"));

        if (!latestTransaction.getId().equals(transaction.getId())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    ErrorCode.CONFLICT,
                    "Only latest transaction can be modified"
            );
        }
    }

    private void rollbackOrThrow(Portfolio portfolio, PortfolioTransaction transaction) {
        try {
            rollbackTransaction(portfolio, transaction);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    ErrorCode.CONFLICT,
                    "Transaction rollback failed",
                    exception.getMessage()
            );
        }
    }

    private void rollbackTransaction(Portfolio portfolio, PortfolioTransaction transaction) {
        switch (transaction.getTransactionType()) {
            case BUY -> rollbackBuy(portfolio, transaction);
            case SELL -> rollbackSell(portfolio, transaction);
            case DEPOSIT, DIVIDEND -> portfolio.applyCashDelta(transaction.getAmount().negate());
            case WITHDRAW, FEE_ADJUST -> portfolio.applyCashDelta(transaction.getAmount());
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "Unsupported transaction type");
        }
    }

    private void rollbackBuy(Portfolio portfolio, PortfolioTransaction transaction) {
        Instrument instrument = requireTransactionInstrument(transaction);
        BigDecimal quantity = requirePositive(transaction.getQuantity(), "quantity");

        Holding holding = holdingRepository.findByPortfolioIdAndInstrumentId(portfolio.getId(), instrument.getId())
                .orElseThrow(() -> new IllegalArgumentException("No holding found for rollback buy transaction"));

        holding.applySell(quantity);
        if (holding.isEmpty()) {
            holdingRepository.delete(holding);
        } else {
            holdingRepository.save(holding);
        }

        portfolio.applyCashDelta(transaction.getAmount());
    }

    private void rollbackSell(Portfolio portfolio, PortfolioTransaction transaction) {
        Instrument instrument = requireTransactionInstrument(transaction);
        BigDecimal quantity = requirePositive(transaction.getQuantity(), "quantity");
        BigDecimal realizedPnl = transaction.getRealizedPnl();

        if (realizedPnl == null) {
            throw new IllegalArgumentException("realizedPnl is required for sell rollback");
        }

        BigDecimal costBasis = normalizeMoney(transaction.getAmount().subtract(realizedPnl));
        if (costBasis.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid cost basis for sell rollback");
        }

        Holding holding = holdingRepository.findByPortfolioIdAndInstrumentId(portfolio.getId(), instrument.getId())
                .orElseGet(() -> new Holding(portfolio, instrument));

        holding.applyBuy(quantity, costBasis);
        holdingRepository.save(holding);

        portfolio.applyCashDelta(transaction.getAmount().negate());
    }

    private static Instrument requireTransactionInstrument(PortfolioTransaction transaction) {
        if (transaction.getInstrument() == null) {
            throw new IllegalArgumentException(
                    "instrument is required for transaction type " + transaction.getTransactionType()
            );
        }
        return transaction.getInstrument();
    }

    private Instrument loadInstrumentForUpdate(TransactionUpdateRequest request, PortfolioTransaction originalTransaction) {
        Long instrumentId = request.instrumentId();
        if (instrumentId == null) {
            return requireTransactionInstrument(originalTransaction);
        }
        return instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Instrument not found: " + instrumentId));
    }

    private static boolean requiresInstrument(TransactionType transactionType) {
        return transactionType == TransactionType.BUY || transactionType == TransactionType.SELL;
    }

    private Instrument loadInstrumentIfNeeded(RegisterTransactionCommand command) {
        if (command.instrumentId() == null) {
            return null;
        }
        return instrumentRepository.findById(command.instrumentId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Instrument not found: " + command.instrumentId()));
    }

    private void ensureInstrumentExists(RegisterTransactionCommand command, Instrument instrument) {
        if (instrument == null) {
            throw badRequestForField(
                    "instrumentId",
                    "instrumentId is required for transaction type " + command.transactionType()
            );
        }
    }

    private static BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static BigDecimal requirePositive(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw badRequestForField(fieldName, fieldName + " must be greater than zero");
        }
        return normalizeMoney(value);
    }

    private static void ensureNull(Object value, String fieldName, TransactionType transactionType) {
        if (value != null) {
            throw badRequestForField(
                    fieldName,
                    fieldName + " must be null for transaction type " + transactionType
            );
        }
    }

    private static BigDecimal requireNonNegative(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw badRequestForField(fieldName, fieldName + " must be greater than or equal to zero");
        }
        return normalizeMoney(value);
    }

    private static ApiException badRequestForField(String fieldName, String message) {
        return new ApiException(
                HttpStatus.BAD_REQUEST,
                ErrorCode.BAD_REQUEST,
                message,
                Map.of(fieldName, message)
        );
    }

    private static BigDecimal normalizeMoney(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
