package com.quant.mvp.pipeline.service;

import com.quant.mvp.pipeline.domain.DrCr;
import com.quant.mvp.pipeline.domain.Order;
import com.quant.mvp.pipeline.domain.OrderSide;
import com.quant.mvp.pipeline.domain.Trade;
import com.quant.mvp.pipeline.payload.CreateVoucherPayload;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoDataInitializer implements ApplicationRunner {

    private final OrderTradePositionPipelineService pipelineService;
    private final JournalLedgerService journalLedgerService;

    public DemoDataInitializer(
            OrderTradePositionPipelineService pipelineService,
            JournalLedgerService journalLedgerService
    ) {
        this.pipelineService = pipelineService;
        this.journalLedgerService = journalLedgerService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!pipelineService.getOrders().isEmpty()) {
            return;
        }

        // portfolio 1
        Order aaplBuyOrder = pipelineService.createOrder(1L, "AAPL", OrderSide.BUY, new BigDecimal("50"));
        Trade aaplBuyTrade = pipelineService.applyTrade(aaplBuyOrder.orderId(), new BigDecimal("50"), new BigDecimal("182.15"));

        Order msftBuyOrder = pipelineService.createOrder(1L, "MSFT", OrderSide.BUY, new BigDecimal("30"));
        pipelineService.applyTrade(msftBuyOrder.orderId(), new BigDecimal("20"), new BigDecimal("417.95"));

        Order nvdaBuyOrder = pipelineService.createOrder(1L, "NVDA", OrderSide.BUY, new BigDecimal("10"));
        pipelineService.applyTrade(nvdaBuyOrder.orderId(), new BigDecimal("10"), new BigDecimal("742.30"));

        Order aaplSellOrder = pipelineService.createOrder(1L, "AAPL", OrderSide.SELL, new BigDecimal("15"));
        Trade aaplSellTrade = pipelineService.applyTrade(aaplSellOrder.orderId(), new BigDecimal("15"), new BigDecimal("189.40"));

        // portfolio 2
        Order jnjBuyOrder = pipelineService.createOrder(2L, "JNJ", OrderSide.BUY, new BigDecimal("40"));
        pipelineService.applyTrade(jnjBuyOrder.orderId(), new BigDecimal("40"), new BigDecimal("159.40"));

        Order pgBuyOrder = pipelineService.createOrder(2L, "PG", OrderSide.BUY, new BigDecimal("45"));
        Trade pgBuyTrade = pipelineService.applyTrade(pgBuyOrder.orderId(), new BigDecimal("45"), new BigDecimal("167.20"));

        // portfolio 3
        Order vixyBuyOrder = pipelineService.createOrder(3L, "VIXY", OrderSide.BUY, new BigDecimal("100"));
        pipelineService.applyTrade(vixyBuyOrder.orderId(), new BigDecimal("100"), new BigDecimal("22.30"));

        // posted voucher: AAPL buy
        var buyVoucherReq = new CreateVoucherPayload.Req(
                1L,
                aaplBuyTrade.tradeId(),
                "AAPL buy posting",
                List.of(
                        new CreateVoucherPayload.EntryReq("STOCK_ASSET", DrCr.DR, new BigDecimal("9107.50"), "AAPL", "asset increase"),
                        new CreateVoucherPayload.EntryReq("CASH", DrCr.CR, new BigDecimal("9107.50"), null, "cash out")
                )
        );
        var postedBuyVoucher = journalLedgerService.createVoucher(buyVoucherReq);
        postedBuyVoucher = journalLedgerService.approve(postedBuyVoucher.voucherId());
        journalLedgerService.post(postedBuyVoucher.voucherId());

        // posted voucher: AAPL sell
        var sellVoucherReq = new CreateVoucherPayload.Req(
                1L,
                aaplSellTrade.tradeId(),
                "AAPL sell posting",
                List.of(
                        new CreateVoucherPayload.EntryReq("CASH", DrCr.DR, new BigDecimal("2841.00"), null, "cash in"),
                        new CreateVoucherPayload.EntryReq("STOCK_ASSET", DrCr.CR, new BigDecimal("2732.25"), "AAPL", "asset decrease"),
                        new CreateVoucherPayload.EntryReq("REALIZED_PNL", DrCr.CR, new BigDecimal("108.75"), null, "realized gain")
                )
        );
        var postedSellVoucher = journalLedgerService.createVoucher(sellVoucherReq);
        postedSellVoucher = journalLedgerService.approve(postedSellVoucher.voucherId());
        journalLedgerService.post(postedSellVoucher.voucherId());

        // approved only voucher
        var approvedReq = new CreateVoucherPayload.Req(
                2L,
                pgBuyTrade.tradeId(),
                "PG buy waiting post",
                List.of(
                        new CreateVoucherPayload.EntryReq("STOCK_ASSET", DrCr.DR, new BigDecimal("7524.00"), "PG", "asset increase"),
                        new CreateVoucherPayload.EntryReq("CASH", DrCr.CR, new BigDecimal("7524.00"), null, "cash out")
                )
        );
        var approved = journalLedgerService.createVoucher(approvedReq);
        journalLedgerService.approve(approved.voucherId());

        // draft voucher
        journalLedgerService.createVoucher(new CreateVoucherPayload.Req(
                1L,
                null,
                "manual adjustment draft",
                List.of(
                        new CreateVoucherPayload.EntryReq("FEE_EXPENSE", DrCr.DR, new BigDecimal("50.00"), null, "fee"),
                        new CreateVoucherPayload.EntryReq("CASH", DrCr.CR, new BigDecimal("50.00"), null, "offset")
                )
        ));
    }
}
