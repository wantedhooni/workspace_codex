package com.derivops.mvp.stockpurchase.application;

import com.derivops.mvp.account.Account;
import com.derivops.mvp.account.AccountStatus;
import com.derivops.mvp.account.infrastructure.AccountRepository;
import com.derivops.mvp.audit.application.AuditLogService;
import com.derivops.mvp.common.BadRequestException;
import com.derivops.mvp.common.NotFoundException;
import com.derivops.mvp.journalentry.JournalEntry;
import com.derivops.mvp.journalentry.infrastructure.JournalEntryRepository;
import com.derivops.mvp.ledger.LedgerEntry;
import com.derivops.mvp.ledger.infrastructure.LedgerEntryRepository;
import com.derivops.mvp.stockposition.StockPosition;
import com.derivops.mvp.stockposition.infrastructure.StockPositionRepository;
import com.derivops.mvp.stockpurchase.StockPurchase;
import com.derivops.mvp.stockpurchase.dto.CreateStockPurchaseRequest;
import com.derivops.mvp.stockpurchase.dto.StockPurchaseResponse;
import com.derivops.mvp.stockpurchase.infrastructure.StockPurchaseRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class StockPurchaseService {

    private static final String REFERENCE_TYPE = "STOCK_PURCHASE";

    private final StockPurchaseRepository stockPurchaseRepository;
    private final StockPositionRepository stockPositionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final AccountRepository accountRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<StockPurchaseResponse> list(Long accountId, String symbol, String keyword, String filter, Pageable pageable) {
        return stockPurchaseRepository.search(accountId, symbol, keyword, filter, pageable).map(this::toResponse);
    }

    @Transactional
    public StockPurchaseResponse create(CreateStockPurchaseRequest request, String actor) {
        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new NotFoundException("Account not found: " + request.accountId()));
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Stock purchase account is not active: " + account.getId());
        }

        BigDecimal quantity = positive(request.quantity(), "quantity");
        BigDecimal price = positive(request.price(), "price");
        BigDecimal feeAmount = nonNegative(request.feeAmount(), "feeAmount");

        String symbol = normalizeCode(request.symbol(), "symbol");
        String market = normalizeCode(request.market(), "market");
        String currency = normalizeCode(request.currency(), "currency");
        LocalDate tradeDate = request.tradeDate();
        LocalDate settlementDate = request.settlementDate() == null ? tradeDate.plusDays(2) : request.settlementDate();
        if (settlementDate.isBefore(tradeDate)) {
            throw new BadRequestException("settlementDate must be on or after tradeDate");
        }

        BigDecimal grossAmount = quantity.multiply(price).setScale(4, RoundingMode.HALF_UP);
        BigDecimal netAmount = grossAmount.add(feeAmount).setScale(4, RoundingMode.HALF_UP);

        StockPurchase purchase = new StockPurchase();
        purchase.setAccount(account);
        purchase.setSymbol(symbol);
        purchase.setMarket(market);
        purchase.setCurrency(currency);
        purchase.setTradeDate(tradeDate);
        purchase.setSettlementDate(settlementDate);
        purchase.setQuantity(quantity);
        purchase.setPrice(price);
        purchase.setGrossAmount(grossAmount);
        purchase.setFeeAmount(feeAmount);
        purchase.setNetAmount(netAmount);
        purchase.setBrokerOrderNo(nextBrokerOrderNo(symbol));
        purchase.setCreatedBy(actor);
        purchase = stockPurchaseRepository.save(purchase);

        StockPosition position = stockPositionRepository.findByAccountIdAndSymbol(account.getId(), symbol)
                .orElseGet(() -> initializePosition(account, symbol, market, currency, tradeDate));
        BigDecimal currentQuantity = position.getQuantity();
        BigDecimal currentCost = position.getTotalCost();
        BigDecimal updatedQuantity = currentQuantity.add(quantity).setScale(4, RoundingMode.HALF_UP);
        BigDecimal updatedCost = currentCost.add(netAmount).setScale(4, RoundingMode.HALF_UP);
        position.setQuantity(updatedQuantity);
        position.setTotalCost(updatedCost);
        position.setAveragePrice(updatedCost.divide(updatedQuantity, 6, RoundingMode.HALF_UP));
        position.setLastTradeDate(tradeDate);
        position.setMarket(market);
        position.setCurrency(currency);
        stockPositionRepository.save(position);

        String referenceId = String.valueOf(purchase.getId());
        ledgerEntryRepository.save(buildLedgerEntry(account, purchase, updatedQuantity, updatedCost, referenceId));

        String journalNo = nextJournalNo(tradeDate);
        int lineNo = 1;
        journalEntryRepository.save(buildJournalEntry(account, journalNo, lineNo++, referenceId, tradeDate, currency,
                "STOCK_INVENTORY", grossAmount, BigDecimal.ZERO,
                "%s buy inventory recognition".formatted(symbol)));
        if (feeAmount.compareTo(BigDecimal.ZERO) > 0) {
            journalEntryRepository.save(buildJournalEntry(account, journalNo, lineNo++, referenceId, tradeDate, currency,
                    "TRADING_FEE_EXPENSE", feeAmount, BigDecimal.ZERO,
                    "%s buy fee".formatted(symbol)));
        }
        journalEntryRepository.save(buildJournalEntry(account, journalNo, lineNo, referenceId, tradeDate, currency,
                "CASH", BigDecimal.ZERO, netAmount,
                "%s purchase cash settlement".formatted(symbol)));

        auditLogService.log(actor, "CREATE_STOCK_PURCHASE", REFERENCE_TYPE, referenceId, detail(purchase));
        return toResponse(purchase);
    }

    private StockPosition initializePosition(Account account, String symbol, String market, String currency, LocalDate tradeDate) {
        StockPosition position = new StockPosition();
        position.setAccount(account);
        position.setSymbol(symbol);
        position.setMarket(market);
        position.setCurrency(currency);
        position.setQuantity(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
        position.setAveragePrice(BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP));
        position.setTotalCost(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
        position.setLastTradeDate(tradeDate);
        return position;
    }

    private LedgerEntry buildLedgerEntry(Account account, StockPurchase purchase, BigDecimal runningQuantity, BigDecimal runningAmount, String referenceId) {
        LedgerEntry entry = new LedgerEntry();
        entry.setEntryNo(nextLedgerEntryNo(purchase.getTradeDate()));
        entry.setAccount(account);
        entry.setReferenceType(REFERENCE_TYPE);
        entry.setReferenceId(referenceId);
        entry.setSymbol(purchase.getSymbol());
        entry.setPostingDate(purchase.getTradeDate());
        entry.setCurrency(purchase.getCurrency());
        entry.setQuantityChange(purchase.getQuantity());
        entry.setAmountChange(purchase.getNetAmount());
        entry.setRunningQuantity(runningQuantity);
        entry.setRunningAmount(runningAmount);
        entry.setDescription("%s buy booked qty=%s price=%s".formatted(
                purchase.getSymbol(),
                purchase.getQuantity(),
                purchase.getPrice()
        ));
        return entry;
    }

    private JournalEntry buildJournalEntry(
            Account account,
            String journalNo,
            int lineNo,
            String referenceId,
            LocalDate postingDate,
            String currency,
            String accountCode,
            BigDecimal debitAmount,
            BigDecimal creditAmount,
            String description
    ) {
        JournalEntry entry = new JournalEntry();
        entry.setJournalNo(journalNo);
        entry.setLineNo(lineNo);
        entry.setAccount(account);
        entry.setReferenceType(REFERENCE_TYPE);
        entry.setReferenceId(referenceId);
        entry.setPostingDate(postingDate);
        entry.setAccountCode(accountCode);
        entry.setDebitAmount(debitAmount.setScale(4, RoundingMode.HALF_UP));
        entry.setCreditAmount(creditAmount.setScale(4, RoundingMode.HALF_UP));
        entry.setCurrency(currency);
        entry.setDescription(description);
        return entry;
    }

    private BigDecimal positive(BigDecimal value, String label) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(label + " must be greater than zero");
        }
        return value;
    }

    private BigDecimal nonNegative(BigDecimal value, String label) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(label + " must be zero or greater");
        }
        return value.setScale(4, RoundingMode.HALF_UP);
    }

    private String normalizeCode(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(label + " is required");
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String nextBrokerOrderNo(String symbol) {
        return "SP-%s-%s".formatted(
                OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")),
                symbol
        );
    }

    private String nextLedgerEntryNo(LocalDate postingDate) {
        return "LE-%s-%s".formatted(postingDate.format(DateTimeFormatter.BASIC_ISO_DATE), UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
    }

    private String nextJournalNo(LocalDate postingDate) {
        return "JE-%s-%s".formatted(postingDate.format(DateTimeFormatter.BASIC_ISO_DATE), UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
    }

    private String detail(StockPurchase purchase) {
        return "accountId=%s symbol=%s market=%s quantity=%s price=%s netAmount=%s".formatted(
                purchase.getAccount().getId(),
                purchase.getSymbol(),
                purchase.getMarket(),
                purchase.getQuantity(),
                purchase.getPrice(),
                purchase.getNetAmount()
        );
    }

    private StockPurchaseResponse toResponse(StockPurchase purchase) {
        return new StockPurchaseResponse(
                purchase.getId(),
                purchase.getAccount().getId(),
                purchase.getAccount().getAccountNo(),
                purchase.getSymbol(),
                purchase.getMarket(),
                purchase.getCurrency(),
                purchase.getTradeDate(),
                purchase.getSettlementDate(),
                purchase.getQuantity(),
                purchase.getPrice(),
                purchase.getGrossAmount(),
                purchase.getFeeAmount(),
                purchase.getNetAmount(),
                purchase.getBrokerOrderNo(),
                purchase.getCreatedBy(),
                purchase.getCreatedAt()
        );
    }
}
