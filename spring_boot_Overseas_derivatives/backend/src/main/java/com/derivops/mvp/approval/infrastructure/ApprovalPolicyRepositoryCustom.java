package com.derivops.mvp.approval.infrastructure;
import com.derivops.mvp.approval.*;
import com.derivops.mvp.approval.api.*;
import com.derivops.mvp.approval.application.*;
import com.derivops.mvp.approval.dto.*;


import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApprovalPolicyRepositoryCustom {
    Page<ApprovalPolicy> search(String keyword, String filter, Pageable pageable);

    Optional<ApprovalPolicy> resolve(String brokerCode, ApprovalDomain domain, LocalDate targetDate);
}
