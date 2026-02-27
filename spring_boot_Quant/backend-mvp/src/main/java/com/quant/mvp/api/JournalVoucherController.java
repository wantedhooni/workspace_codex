package com.quant.mvp.api;

import com.quant.mvp.pipeline.domain.JournalVoucher;
import com.quant.mvp.pipeline.domain.PermissionAction;
import com.quant.mvp.pipeline.payload.CreateVoucherPayload;
import com.quant.mvp.pipeline.payload.VoucherActionPayload;
import com.quant.mvp.pipeline.payload.VoucherListPayload;
import com.quant.mvp.pipeline.service.JournalLedgerService;
import jakarta.validation.Valid;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/journal-vouchers")
public class JournalVoucherController {

    private final JournalLedgerService journalLedgerService;
    private final PermissionGuard permissionGuard;

    public JournalVoucherController(
            JournalLedgerService journalLedgerService,
            PermissionGuard permissionGuard
    ) {
        this.journalLedgerService = journalLedgerService;
        this.permissionGuard = permissionGuard;
    }

    @GetMapping
    public VoucherListPayload.Res list(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestParam(required = false) Long portfolioId,
            @RequestParam(required = false) String status
    ) {
        permissionGuard.require(userEmail, "journalVouchers", PermissionAction.READ);
        return new VoucherListPayload.Res(
                journalLedgerService.searchVouchers(portfolioId, status).stream()
                        .map(v -> new VoucherListPayload.Item(
                                v.voucherId(),
                                v.voucherNo(),
                                v.portfolioId(),
                                v.tradeId(),
                                v.status(),
                                v.createdAt(),
                                v.approvedAt(),
                                v.postedAt()
                        ))
                        .collect(Collectors.toList())
        );
    }

    @PostMapping
    public CreateVoucherPayload.Res create(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @Valid @RequestBody CreateVoucherPayload.Req req
    ) {
        permissionGuard.require(userEmail, "journalVouchers", PermissionAction.CREATE);
        JournalVoucher voucher = journalLedgerService.createVoucher(req);
        return new CreateVoucherPayload.Res(
                voucher.voucherId(),
                voucher.voucherNo(),
                voucher.portfolioId(),
                voucher.tradeId(),
                voucher.status(),
                voucher.createdAt()
        );
    }

    @PostMapping("/{voucherId}/approve")
    public VoucherActionPayload.Res approve(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @PathVariable Long voucherId,
            @RequestBody(required = false) VoucherActionPayload.Req req
    ) {
        permissionGuard.require(userEmail, "journalVouchers", PermissionAction.UPDATE);
        JournalVoucher voucher = journalLedgerService.approve(voucherId);
        return toRes(voucher);
    }

    @PostMapping("/{voucherId}/post")
    public VoucherActionPayload.Res post(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @PathVariable Long voucherId,
            @RequestBody(required = false) VoucherActionPayload.Req req
    ) {
        permissionGuard.require(userEmail, "journalVouchers", PermissionAction.UPDATE);
        JournalVoucher voucher = journalLedgerService.post(voucherId);
        return toRes(voucher);
    }

    @PostMapping("/{voucherId}/cancel")
    public VoucherActionPayload.Res cancel(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @PathVariable Long voucherId,
            @RequestBody(required = false) VoucherActionPayload.Req req
    ) {
        permissionGuard.require(userEmail, "journalVouchers", PermissionAction.UPDATE);
        JournalVoucher voucher = journalLedgerService.cancel(voucherId);
        return toRes(voucher);
    }

    private VoucherActionPayload.Res toRes(JournalVoucher voucher) {
        return new VoucherActionPayload.Res(
                voucher.voucherId(),
                voucher.voucherNo(),
                voucher.status(),
                voucher.approvedAt(),
                voucher.postedAt()
        );
    }
}
