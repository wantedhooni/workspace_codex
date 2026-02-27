package com.quant.mvp.api;

import com.quant.mvp.pipeline.domain.LedgerEntry;
import com.quant.mvp.pipeline.domain.LedgerValidationResult;
import com.quant.mvp.pipeline.domain.PermissionAction;
import com.quant.mvp.pipeline.payload.LedgerEntrySearchPayload;
import com.quant.mvp.pipeline.payload.LedgerValidationPayload;
import com.quant.mvp.pipeline.service.JournalLedgerService;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ledgers")
public class LedgerController {

    private final JournalLedgerService journalLedgerService;
    private final PermissionGuard permissionGuard;

    public LedgerController(
            JournalLedgerService journalLedgerService,
            PermissionGuard permissionGuard
    ) {
        this.journalLedgerService = journalLedgerService;
        this.permissionGuard = permissionGuard;
    }

    @GetMapping("/entries")
    public LedgerEntrySearchPayload.Res entries(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestParam(required = false) Long portfolioId,
            @RequestParam(required = false) Long voucherId,
            @RequestParam(required = false) String accountCode
    ) {
        permissionGuard.require(userEmail, "ledgerEntries", PermissionAction.READ);
        return new LedgerEntrySearchPayload.Res(
                journalLedgerService.searchLedgerEntries(portfolioId, voucherId, accountCode)
                        .stream()
                        .map(this::toItem)
                        .collect(Collectors.toList())
        );
    }

    @PostMapping("/validate")
    public LedgerValidationPayload.Res validateNow(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail
    ) {
        permissionGuard.require(userEmail, "ledgerEntries", PermissionAction.UPDATE);
        return toRes(journalLedgerService.validateLedger());
    }

    @GetMapping("/validate/last")
    public LedgerValidationPayload.Res lastValidation(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail
    ) {
        permissionGuard.require(userEmail, "ledgerEntries", PermissionAction.READ);
        return toRes(journalLedgerService.lastValidation());
    }

    private LedgerEntrySearchPayload.Item toItem(LedgerEntry e) {
        return new LedgerEntrySearchPayload.Item(
                e.ledgerEntryId(),
                e.voucherId(),
                e.portfolioId(),
                e.accountCode(),
                e.drCr(),
                e.amount(),
                e.symbol(),
                e.description(),
                e.createdAt()
        );
    }

    private LedgerValidationPayload.Res toRes(LedgerValidationResult result) {
        return new LedgerValidationPayload.Res(
                result.validatedAt(),
                result.postedVoucherCount(),
                result.totalDebit(),
                result.totalCredit(),
                result.balanced(),
                result.message()
        );
    }
}
