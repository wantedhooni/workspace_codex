package com.derivops.mvp.opscase.infrastructure;
import com.derivops.mvp.opscase.*;
import com.derivops.mvp.opscase.api.*;
import com.derivops.mvp.opscase.application.*;
import com.derivops.mvp.opscase.dto.*;


import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpsCaseRepository extends JpaRepository<OpsCase, Long>, OpsCaseRepositoryCustom {
    Optional<OpsCase> findFirstByLinkedTypeAndLinkedIdAndStatusInOrderByCreatedAtDesc(
            String linkedType,
            String linkedId,
            List<OpsCaseStatus> statuses
    );
}
