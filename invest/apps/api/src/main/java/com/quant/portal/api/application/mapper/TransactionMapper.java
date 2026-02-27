package com.quant.portal.api.application.mapper;

import com.quant.portal.api.application.service.command.RegisterTransactionCommand;
import com.quant.portal.api.presentation.dto.transaction.TransactionCreateRequest;
import com.quant.portal.api.presentation.dto.transaction.TransactionResponse;
import com.quant.portal.domain.portfolio.entity.PortfolioTransaction;

public final class TransactionMapper {

    private TransactionMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static RegisterTransactionCommand toCommand(TransactionCreateRequest request) {
        if (request == null) {
            return null;
        }
        return new RegisterTransactionCommand(
                request.portfolioId(),
                request.instrumentId(),
                request.transactionType(),
                request.tradeDate(),
                request.quantity(),
                request.unitPrice(),
                request.amount(),
                request.fee(),
                request.tax(),
                request.currencyCode(),
                request.memo()
        );
    }

    public static TransactionResponse toDto(PortfolioTransaction transaction) {
        if (transaction == null) {
            return null;
        }
        return new TransactionResponse(
                transaction.getId(),
                transaction.getPortfolio().getId(),
                transaction.getInstrument() == null ? null : transaction.getInstrument().getId(),
                transaction.getTransactionType(),
                transaction.getTradeDate(),
                transaction.getQuantity(),
                transaction.getUnitPrice(),
                transaction.getAmount(),
                transaction.getFee(),
                transaction.getTax(),
                transaction.getCashImpact(),
                transaction.getRealizedPnl(),
                transaction.getCurrencyCode(),
                transaction.getMemo()
        );
    }
}
