package com.derivops.mvp.approval.infrastructure;
import com.derivops.mvp.approval.*;
import com.derivops.mvp.approval.api.*;
import com.derivops.mvp.approval.application.*;
import com.derivops.mvp.approval.dto.*;


import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalPolicyRepository extends JpaRepository<ApprovalPolicy, Long>, ApprovalPolicyRepositoryCustom {
}
