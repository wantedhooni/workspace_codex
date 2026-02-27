package com.quant.mvp.pipeline.service;

import com.quant.mvp.pipeline.domain.DrCr;
import com.quant.mvp.pipeline.domain.JournalEntryLine;
import com.quant.mvp.pipeline.domain.JournalVoucher;
import com.quant.mvp.pipeline.domain.LedgerEntry;
import com.quant.mvp.pipeline.domain.LedgerValidationResult;
import com.quant.mvp.pipeline.domain.OrderSide;
import com.quant.mvp.pipeline.domain.VoucherStatus;
import com.quant.mvp.pipeline.payload.CreateVoucherPayload;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class JournalLedgerService {

    private static final Set<String> ALLOWED_ACCOUNTS = Set.of(
            "CASH",
            "STOCK_ASSET",
            "FEE_EXPENSE",
            "TAX_EXPENSE",
            "REALIZED_PNL",
            "UNREALIZED_PNL"
    );

    private final AtomicLong voucherSeq = new AtomicLong(2000);
    private final AtomicLong ledgerSeq = new AtomicLong(9000);

    private final Map<Long, JournalVoucher> voucherStore = new ConcurrentHashMap<>();
    private final Map<Long, List<LedgerEntry>> ledgerEntriesByVoucher = new ConcurrentHashMap<>();
    private volatile LedgerValidationResult lastValidation = new LedgerValidationResult(
            Instant.now(), 0, BigDecimal.ZERO, BigDecimal.ZERO, true, "no posted voucher");

    private final OrderTradePositionPipelineService pipelineService;

    public JournalLedgerService(OrderTradePositionPipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    public synchronized JournalVoucher createVoucher(CreateVoucherPayload.Req req) {
        if (req.portfolioId() == null || req.portfolioId() <= 0) {
            throw new IllegalArgumentException("portfolioId must be positive");
        }
        if (req.entries() == null || req.entries().size() < 2) {
            throw new IllegalArgumentException("voucher requires at least 2 entries");
        }

        List<JournalEntryLine> lines = new ArrayList<>();
        int lineNo = 1;
        for (CreateVoucherPayload.EntryReq entry : req.entries()) {
            lines.add(normalizeEntry(lineNo++, entry));
        }

        assertBalanced(lines);

        if (req.tradeId() != null) {
            validateTradeLinkedVoucher(req.portfolioId(), req.tradeId(), lines);
        }

        Long voucherId = voucherSeq.incrementAndGet();
        String voucherNo = "JV-" + LocalDate.now() + "-" + voucherId;

        JournalVoucher voucher = new JournalVoucher(
                voucherId,
                voucherNo,
                req.portfolioId(),
                req.tradeId(),
                VoucherStatus.DRAFT,
                List.copyOf(lines),
                req.description().trim(),
                Instant.now(),
                null,
                null
        );
        voucherStore.put(voucherId, voucher);
        return voucher;
    }

    public synchronized JournalVoucher approve(Long voucherId) {
        JournalVoucher voucher = requireVoucher(voucherId);
        if (voucher.status() != VoucherStatus.DRAFT) {
            throw new IllegalArgumentException("only DRAFT voucher can be approved");
        }

        if (voucher.tradeId() != null && hasActiveVoucherForTrade(voucher.tradeId(), voucher.voucherId())) {
            throw new IllegalArgumentException("another active voucher already exists for tradeId=" + voucher.tradeId());
        }

        JournalVoucher approved = voucher.approve();
        voucherStore.put(voucherId, approved);
        return approved;
    }

    public synchronized JournalVoucher post(Long voucherId) {
        JournalVoucher voucher = requireVoucher(voucherId);
        if (voucher.status() != VoucherStatus.APPROVED) {
            throw new IllegalArgumentException("only APPROVED voucher can be posted");
        }
        assertBalanced(voucher.entries());

        if (voucher.tradeId() != null && hasPostedVoucherForTrade(voucher.tradeId(), voucher.voucherId())) {
            throw new IllegalArgumentException("posted voucher already exists for tradeId=" + voucher.tradeId());
        }

        List<LedgerEntry> created = new ArrayList<>();
        for (JournalEntryLine line : voucher.entries()) {
            created.add(new LedgerEntry(
                    ledgerSeq.incrementAndGet(),
                    voucher.voucherId(),
                    voucher.portfolioId(),
                    line.accountCode(),
                    line.drCr(),
                    line.amount(),
                    line.symbol(),
                    line.description(),
                    Instant.now()
            ));
        }
        ledgerEntriesByVoucher.put(voucher.voucherId(), created);

        JournalVoucher posted = voucher.post();
        voucherStore.put(voucherId, posted);
        return posted;
    }

    public synchronized JournalVoucher cancel(Long voucherId) {
        JournalVoucher voucher = requireVoucher(voucherId);
        if (voucher.status() == VoucherStatus.CANCELED) {
            return voucher;
        }
        if (voucher.status() == VoucherStatus.POSTED) {
            throw new IllegalArgumentException("POSTED voucher cannot be canceled directly. use reverse voucher");
        }
        JournalVoucher canceled = voucher.cancel();
        voucherStore.put(voucherId, canceled);
        return canceled;
    }

    public List<LedgerEntry> searchLedgerEntries(Long portfolioId, Long voucherId) {
        return ledgerEntriesByVoucher.values().stream()
                .flatMap(List::stream)
                .filter(e -> portfolioId == null || Objects.equals(e.portfolioId(), portfolioId))
                .filter(e -> voucherId == null || Objects.equals(e.voucherId(), voucherId))
                .sorted(Comparator.comparing(LedgerEntry::ledgerEntryId))
                .toList();
    }

    public List<LedgerEntry> searchLedgerEntries(Long portfolioId, Long voucherId, String accountCode) {
        return ledgerEntriesByVoucher.values().stream()
                .flatMap(List::stream)
                .filter(e -> portfolioId == null || Objects.equals(e.portfolioId(), portfolioId))
                .filter(e -> voucherId == null || Objects.equals(e.voucherId(), voucherId))
                .filter(e -> accountCode == null || accountCode.isBlank() || e.accountCode().equalsIgnoreCase(accountCode.trim()))
                .sorted(Comparator.comparing(LedgerEntry::ledgerEntryId))
                .toList();
    }

    public synchronized LedgerValidationResult validateLedger() {
        List<JournalVoucher> postedVouchers = voucherStore.values().stream()
                .filter(v -> v.status() == VoucherStatus.POSTED)
                .toList();

        BigDecimal totalDr = BigDecimal.ZERO;
        BigDecimal totalCr = BigDecimal.ZERO;

        for (JournalVoucher v : postedVouchers) {
            List<LedgerEntry> entries = ledgerEntriesByVoucher.getOrDefault(v.voucherId(), List.of());
            for (LedgerEntry e : entries) {
                if (e.drCr() == DrCr.DR) {
                    totalDr = totalDr.add(e.amount());
                } else {
                    totalCr = totalCr.add(e.amount());
                }
            }
        }

        boolean balanced = totalDr.compareTo(totalCr) == 0;
        String msg = balanced ? "ledger balanced" : "ledger NOT balanced";

        LedgerValidationResult result = new LedgerValidationResult(
                Instant.now(),
                postedVouchers.size(),
                totalDr,
                totalCr,
                balanced,
                msg
        );
        lastValidation = result;
        return result;
    }

    public LedgerValidationResult lastValidation() {
        return lastValidation;
    }

    public List<JournalVoucher> getVouchers() {
        return voucherStore.values().stream()
                .sorted(Comparator.comparing(JournalVoucher::voucherId))
                .toList();
    }

    public List<JournalVoucher> searchVouchers(Long portfolioId, String status) {
        return voucherStore.values().stream()
                .filter(v -> portfolioId == null || Objects.equals(v.portfolioId(), portfolioId))
                .filter(v -> status == null || status.isBlank() || v.status().name().equalsIgnoreCase(status.trim()))
                .sorted(Comparator.comparing(JournalVoucher::voucherId))
                .toList();
    }

    private JournalVoucher requireVoucher(Long voucherId) {
        JournalVoucher voucher = voucherStore.get(voucherId);
        if (voucher == null) {
            throw new IllegalArgumentException("voucher not found: " + voucherId);
        }
        return voucher;
    }

    private JournalEntryLine normalizeEntry(int lineNo, CreateVoucherPayload.EntryReq entry) {
        String accountCode = entry.accountCode() == null ? "" : entry.accountCode().trim().toUpperCase();
        if (!ALLOWED_ACCOUNTS.contains(accountCode)) {
            throw new IllegalArgumentException("unsupported accountCode: " + entry.accountCode());
        }

        if (entry.amount() == null || entry.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("entry amount must be positive");
        }

        String normalizedSymbol = entry.symbol() == null || entry.symbol().isBlank()
                ? null
                : entry.symbol().trim().toUpperCase();

        return new JournalEntryLine(
                lineNo,
                accountCode,
                entry.drCr(),
                entry.amount().stripTrailingZeros(),
                normalizedSymbol,
                entry.description()
        );
    }

    private void validateTradeLinkedVoucher(Long portfolioId, Long tradeId, List<JournalEntryLine> lines) {
        OrderTradePositionPipelineService.TradeExecutionView tradeView = pipelineService.requireTradeExecutionView(tradeId);

        if (!Objects.equals(tradeView.portfolioId(), portfolioId)) {
            throw new IllegalArgumentException(
                    "voucher portfolioId does not match trade portfolio. tradeId=" + tradeId
                            + ", tradePortfolio=" + tradeView.portfolioId() + ", voucherPortfolio=" + portfolioId);
        }

        if (hasActiveVoucherForTrade(tradeId, null)) {
            throw new IllegalArgumentException("active voucher already exists for tradeId=" + tradeId);
        }

        assertTradePostingShape(tradeView.trade().side(), tradeView.trade().symbol(), lines);
    }

    private void assertTradePostingShape(OrderSide tradeSide, String symbol, List<JournalEntryLine> lines) {
        BigDecimal stockDr = sum(lines, "STOCK_ASSET", DrCr.DR);
        BigDecimal stockCr = sum(lines, "STOCK_ASSET", DrCr.CR);
        BigDecimal cashDr = sum(lines, "CASH", DrCr.DR);
        BigDecimal cashCr = sum(lines, "CASH", DrCr.CR);

        if (tradeSide == OrderSide.BUY) {
            if (stockDr.compareTo(BigDecimal.ZERO) <= 0 || cashCr.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("BUY trade voucher must include STOCK_ASSET DR and CASH CR entries");
            }
        } else {
            if (cashDr.compareTo(BigDecimal.ZERO) <= 0 || stockCr.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("SELL trade voucher must include CASH DR and STOCK_ASSET CR entries");
            }
        }

        boolean stockLineHasSymbol = lines.stream()
                .anyMatch(line -> line.accountCode().equals("STOCK_ASSET")
                        && line.symbol() != null
                        && line.symbol().equalsIgnoreCase(symbol));

        if (!stockLineHasSymbol) {
            throw new IllegalArgumentException("STOCK_ASSET line must contain trade symbol: " + symbol);
        }
    }

    private BigDecimal sum(List<JournalEntryLine> lines, String accountCode, DrCr drCr) {
        return lines.stream()
                .filter(e -> e.accountCode().equals(accountCode))
                .filter(e -> e.drCr() == drCr)
                .map(JournalEntryLine::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean hasPostedVoucherForTrade(Long tradeId, Long excludeVoucherId) {
        return voucherStore.values().stream()
                .filter(v -> Objects.equals(v.tradeId(), tradeId))
                .filter(v -> excludeVoucherId == null || !Objects.equals(v.voucherId(), excludeVoucherId))
                .anyMatch(v -> v.status() == VoucherStatus.POSTED);
    }

    private boolean hasActiveVoucherForTrade(Long tradeId, Long excludeVoucherId) {
        return voucherStore.values().stream()
                .filter(v -> Objects.equals(v.tradeId(), tradeId))
                .filter(v -> excludeVoucherId == null || !Objects.equals(v.voucherId(), excludeVoucherId))
                .anyMatch(v -> v.status() != VoucherStatus.CANCELED);
    }

    private void assertBalanced(List<JournalEntryLine> entries) {
        BigDecimal dr = entries.stream()
                .filter(e -> e.drCr() == DrCr.DR)
                .map(JournalEntryLine::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal cr = entries.stream()
                .filter(e -> e.drCr() == DrCr.CR)
                .map(JournalEntryLine::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (dr.compareTo(cr) != 0) {
            throw new IllegalArgumentException("voucher entries must be balanced. debit=" + dr + ", credit=" + cr);
        }
    }
}
