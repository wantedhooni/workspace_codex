package com.derivops.mvp.cashfx.infrastructure;
import com.derivops.mvp.cashfx.*;
import com.derivops.mvp.cashfx.api.*;
import com.derivops.mvp.cashfx.application.*;
import com.derivops.mvp.cashfx.dto.*;


import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FxRequestRepositoryCustom {
    Page<FxRequest> search(RequestStatus status, Long accountId, String keyword, String filter, Pageable pageable);

    BigDecimal sumExposure(Long accountId, String fromCurrency, LocalDate valueDate);
}
