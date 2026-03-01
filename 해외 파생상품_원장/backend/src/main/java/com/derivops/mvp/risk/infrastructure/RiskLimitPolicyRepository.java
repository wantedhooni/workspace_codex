package com.derivops.mvp.risk.infrastructure;
import com.derivops.mvp.risk.*;
import com.derivops.mvp.risk.api.*;
import com.derivops.mvp.risk.application.*;
import com.derivops.mvp.risk.dto.*;


import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskLimitPolicyRepository extends JpaRepository<RiskLimitPolicy, Long>, RiskLimitPolicyRepositoryCustom {
}
