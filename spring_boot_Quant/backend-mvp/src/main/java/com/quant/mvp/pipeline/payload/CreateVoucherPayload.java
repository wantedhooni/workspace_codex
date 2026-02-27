package com.quant.mvp.pipeline.payload;

import com.quant.mvp.pipeline.domain.DrCr;
import com.quant.mvp.pipeline.domain.VoucherStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class CreateVoucherPayload {

    private CreateVoucherPayload() {
    }

    public record Req(
            @NotNull Long portfolioId,
            Long tradeId,
            @NotBlank String description,
            @NotEmpty List<@Valid EntryReq> entries
    ) {
    }

    public record EntryReq(
            @NotBlank String accountCode,
            @NotNull DrCr drCr,
            @NotNull @Positive BigDecimal amount,
            String symbol,
            String description
    ) {
    }

    public record Res(
            Long voucherId,
            String voucherNo,
            Long portfolioId,
            Long tradeId,
            VoucherStatus status,
            Instant createdAt
    ) {
    }
}
