package com.quant.mvp.pipeline.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.quant.mvp.pipeline.domain.DrCr;
import com.quant.mvp.pipeline.domain.JournalVoucher;
import com.quant.mvp.pipeline.domain.LedgerValidationResult;
import com.quant.mvp.pipeline.domain.Order;
import com.quant.mvp.pipeline.domain.OrderSide;
import com.quant.mvp.pipeline.domain.Trade;
import com.quant.mvp.pipeline.domain.VoucherStatus;
import com.quant.mvp.pipeline.payload.CreateVoucherPayload;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class JournalLedgerServiceTest {

    @Test
    void voucherApprovePostAndLedgerValidation() {
        OrderTradePositionPipelineService pipelineService = new OrderTradePositionPipelineService();
        Order order = pipelineService.createOrder(1L, "AAPL", OrderSide.BUY, new BigDecimal("10"));
        Trade trade = pipelineService.applyTrade(order.orderId(), new BigDecimal("10"), new BigDecimal("100.00"));

        JournalLedgerService service = new JournalLedgerService(pipelineService);

        CreateVoucherPayload.Req req = new CreateVoucherPayload.Req(
                1L,
                trade.tradeId(),
                "trade posting",
                List.of(
                        new CreateVoucherPayload.EntryReq("STOCK_ASSET", DrCr.DR, new BigDecimal("1000.00"), "AAPL", "buy"),
                        new CreateVoucherPayload.EntryReq("CASH", DrCr.CR, new BigDecimal("1000.00"), null, "buy")
                )
        );

        JournalVoucher created = service.createVoucher(req);
        assertEquals(VoucherStatus.DRAFT, created.status());

        JournalVoucher approved = service.approve(created.voucherId());
        assertEquals(VoucherStatus.APPROVED, approved.status());

        JournalVoucher posted = service.post(created.voucherId());
        assertEquals(VoucherStatus.POSTED, posted.status());

        LedgerValidationResult result = service.validateLedger();
        assertTrue(result.balanced());
        assertEquals(0, result.totalDebit().compareTo(new BigDecimal("1000.00")));
        assertEquals(0, result.totalCredit().compareTo(new BigDecimal("1000.00")));
    }

    @Test
    void tradeLinkedVoucherRejectsDuplicateActiveVoucher() {
        OrderTradePositionPipelineService pipelineService = new OrderTradePositionPipelineService();
        Order order = pipelineService.createOrder(1L, "MSFT", OrderSide.BUY, new BigDecimal("5"));
        Trade trade = pipelineService.applyTrade(order.orderId(), new BigDecimal("5"), new BigDecimal("300.00"));

        JournalLedgerService service = new JournalLedgerService(pipelineService);

        CreateVoucherPayload.Req first = new CreateVoucherPayload.Req(
                1L,
                trade.tradeId(),
                "first",
                List.of(
                        new CreateVoucherPayload.EntryReq("STOCK_ASSET", DrCr.DR, new BigDecimal("1500.00"), "MSFT", "buy"),
                        new CreateVoucherPayload.EntryReq("CASH", DrCr.CR, new BigDecimal("1500.00"), null, "buy")
                )
        );
        service.createVoucher(first);

        CreateVoucherPayload.Req second = new CreateVoucherPayload.Req(
                1L,
                trade.tradeId(),
                "second",
                List.of(
                        new CreateVoucherPayload.EntryReq("STOCK_ASSET", DrCr.DR, new BigDecimal("1500.00"), "MSFT", "buy"),
                        new CreateVoucherPayload.EntryReq("CASH", DrCr.CR, new BigDecimal("1500.00"), null, "buy")
                )
        );

        assertThrows(IllegalArgumentException.class, () -> service.createVoucher(second));
    }

    @Test
    void tradeLinkedVoucherRejectsPortfolioMismatch() {
        OrderTradePositionPipelineService pipelineService = new OrderTradePositionPipelineService();
        Order order = pipelineService.createOrder(1L, "NVDA", OrderSide.BUY, new BigDecimal("3"));
        Trade trade = pipelineService.applyTrade(order.orderId(), new BigDecimal("3"), new BigDecimal("800.00"));

        JournalLedgerService service = new JournalLedgerService(pipelineService);

        CreateVoucherPayload.Req wrongPortfolioReq = new CreateVoucherPayload.Req(
                2L,
                trade.tradeId(),
                "wrong portfolio",
                List.of(
                        new CreateVoucherPayload.EntryReq("STOCK_ASSET", DrCr.DR, new BigDecimal("2400.00"), "NVDA", "buy"),
                        new CreateVoucherPayload.EntryReq("CASH", DrCr.CR, new BigDecimal("2400.00"), null, "buy")
                )
        );

        assertThrows(IllegalArgumentException.class, () -> service.createVoucher(wrongPortfolioReq));
    }
}
