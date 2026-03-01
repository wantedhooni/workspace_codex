package com.derivops.mvp.risk.infrastructure;
import com.derivops.mvp.risk.*;
import com.derivops.mvp.risk.api.*;
import com.derivops.mvp.risk.application.*;
import com.derivops.mvp.risk.dto.*;


import com.derivops.mvp.approval.ApprovalDomain;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RiskLimitPolicyRepositoryCustom {
    Page<RiskLimitPolicy> search(String keyword, String filter, Pageable pageable);

    Optional<RiskLimitPolicy> resolve(String brokerCode, ApprovalDomain domain, String currencyCode, LocalDate targetDate);
}
