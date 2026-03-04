package com.revy.mvpbanking.exchange.presentation;

import com.revy.mvpbanking.exchange.domain.ExchangeRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ExchangeRequestResponse(
        UUID id,
        UUID customerId,
        UUID sourceAccountId,
        UUID destinationAccountId,
        String requestNumber,
        String fromCurrency,
        String toCurrency,
        BigDecimal fromAmount,
        BigDecimal appliedRate,
        BigDecimal toAmount,
        BigDecimal exchangeFeeAmount,
        BigDecimal netToAmount,
        String status,
        String sourceTransactionNumber,
        String destinationTransactionNumber,
        Instant settledAt,
        Instant createdAt
) {
    public static ExchangeRequestResponse from(ExchangeRequest exchangeRequest) {
        return new ExchangeRequestResponse(
                exchangeRequest.getId(),
                exchangeRequest.getCustomerId(),
                exchangeRequest.getSourceAccountId(),
                exchangeRequest.getDestinationAccountId(),
                exchangeRequest.getRequestNumber(),
                exchangeRequest.getFromCurrency(),
                exchangeRequest.getToCurrency(),
                exchangeRequest.getFromAmount(),
                exchangeRequest.getAppliedRate(),
                exchangeRequest.getToAmount(),
                exchangeRequest.getExchangeFeeAmount(),
                exchangeRequest.getNetToAmount(),
                exchangeRequest.getStatus().name(),
                exchangeRequest.getSourceTransactionNumber(),
                exchangeRequest.getDestinationTransactionNumber(),
                exchangeRequest.getSettledAt(),
                exchangeRequest.getCreatedAt()
        );
    }
}
