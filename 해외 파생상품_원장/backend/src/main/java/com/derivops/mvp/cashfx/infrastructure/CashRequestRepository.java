package com.derivops.mvp.cashfx.infrastructure;
import com.derivops.mvp.cashfx.*;
import com.derivops.mvp.cashfx.api.*;
import com.derivops.mvp.cashfx.application.*;
import com.derivops.mvp.cashfx.dto.*;


import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashRequestRepository extends JpaRepository<CashRequest, UUID>, CashRequestRepositoryCustom {
    boolean existsByAccountIdAndTypeAndCurrencyAndAmountAndStatusAndRequestedAtAfter(
            Long accountId,
            CashRequestType type,
            String currency,
            BigDecimal amount,
            RequestStatus status,
            OffsetDateTime requestedAfter
    );
}
