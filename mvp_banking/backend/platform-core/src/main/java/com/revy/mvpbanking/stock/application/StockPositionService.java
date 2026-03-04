package com.revy.mvpbanking.stock.application;

import com.revy.mvpbanking.audit.application.AuditLogService;
import com.revy.mvpbanking.audit.domain.AuditActionType;
import com.revy.mvpbanking.customer.domain.CustomerRepository;
import com.revy.mvpbanking.stock.domain.StockPosition;
import com.revy.mvpbanking.stock.domain.StockQuote;
import com.revy.mvpbanking.stock.domain.StockQuoteRepository;
import com.revy.mvpbanking.stock.domain.StockPositionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional(readOnly = true)
public class StockPositionService {

    private final StockPositionRepository stockPositionRepository;
    private final StockQuoteRepository stockQuoteRepository;
    private final CustomerRepository customerRepository;
    private final AuditLogService auditLogService;

    public StockPositionService(
            StockPositionRepository stockPositionRepository,
            StockQuoteRepository stockQuoteRepository,
            CustomerRepository customerRepository,
            AuditLogService auditLogService
    ) {
        this.stockPositionRepository = stockPositionRepository;
        this.stockQuoteRepository = stockQuoteRepository;
        this.customerRepository = customerRepository;
        this.auditLogService = auditLogService;
    }

    public List<ValuedStockPosition> getAdminPositions() {
        auditLogService.logCurrentActor(AuditActionType.STOCK_POSITION_LIST_VIEWED, "STOCK_POSITION", "all", "Viewed stock positions");
        return getValuedPositions(stockPositionRepository.findAllByOrderByCreatedAtDesc());
    }

    public List<ValuedStockPosition> getUserPositions(UUID endUserId) {
        UUID customerId = customerRepository.findByEndUserId(endUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"))
                .getId();
        auditLogService.logCurrentActor(AuditActionType.STOCK_POSITION_LIST_VIEWED, "STOCK_POSITION", customerId.toString(), "Viewed stock positions");
        return getValuedPositionsByCustomerId(customerId);
    }

    public List<ValuedStockPosition> getValuedPositionsByCustomerId(UUID customerId) {
        return getValuedPositions(stockPositionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId));
    }

    private List<ValuedStockPosition> getValuedPositions(List<StockPosition> stockPositions) {
        return stockPositions.stream()
                .map(this::toValuedPosition)
                .toList();
    }

    private ValuedStockPosition toValuedPosition(StockPosition stockPosition) {
        StockQuote latestQuote = stockQuoteRepository
                .findTopBySymbolIgnoreCaseAndMarketIgnoreCaseOrderByEffectiveAtDesc(stockPosition.getSymbol(), stockPosition.getMarket())
                .orElse(null);
        if (latestQuote != null && !latestQuote.getCurrency().equalsIgnoreCase(stockPosition.getCurrency())) {
            latestQuote = null;
        }

        BigDecimal costBasis = stockPosition.getAveragePrice()
                .multiply(stockPosition.getQuantity())
                .setScale(4, RoundingMode.HALF_UP);

        BigDecimal currentPrice = latestQuote != null ? latestQuote.getPrice() : null;
        BigDecimal marketValue = currentPrice != null
                ? currentPrice.multiply(stockPosition.getQuantity()).setScale(4, RoundingMode.HALF_UP)
                : null;
        BigDecimal unrealizedProfitLoss = marketValue != null
                ? marketValue.subtract(costBasis).setScale(4, RoundingMode.HALF_UP)
                : null;
        BigDecimal unrealizedProfitRate = unrealizedProfitLoss != null && costBasis.signum() > 0
                ? unrealizedProfitLoss.divide(costBasis, 6, RoundingMode.HALF_UP)
                : null;

        return new ValuedStockPosition(
                stockPosition.getId(),
                stockPosition.getCustomerId(),
                stockPosition.getAccountId(),
                stockPosition.getSymbol(),
                stockPosition.getMarket(),
                stockPosition.getQuantity(),
                stockPosition.getAveragePrice(),
                stockPosition.getCurrency(),
                costBasis,
                currentPrice,
                marketValue,
                unrealizedProfitLoss,
                unrealizedProfitRate,
                stockPosition.getRealizedProfitLoss(),
                latestQuote != null ? latestQuote.getChangeRate() : null,
                latestQuote != null ? latestQuote.getEffectiveAt() : null,
                latestQuote != null ? latestQuote.getSource() : null,
                stockPosition.getUpdatedAt()
        );
    }
}
