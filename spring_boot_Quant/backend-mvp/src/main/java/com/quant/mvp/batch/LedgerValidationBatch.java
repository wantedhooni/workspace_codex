package com.quant.mvp.batch;

import com.quant.mvp.pipeline.service.JournalLedgerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LedgerValidationBatch {

    private final JournalLedgerService journalLedgerService;

    public LedgerValidationBatch(JournalLedgerService journalLedgerService) {
        this.journalLedgerService = journalLedgerService;
    }

    @Scheduled(fixedDelayString = "${mvp.ledger.validation-interval-ms:60000}")
    public void run() {
        journalLedgerService.validateLedger();
    }
}
