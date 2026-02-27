package com.quant.portal.api.infrastructure.jpa.repository;

import com.quant.portal.api.infrastructure.jpa.repository.query.MacroIndicatorQueryRepository;
import com.quant.portal.domain.macro.entity.MacroIndicator;
import com.quant.portal.domain.macro.enums.MacroRegionCode;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MacroIndicatorRepository extends JpaRepository<MacroIndicator, Long>, MacroIndicatorQueryRepository {

    Optional<MacroIndicator> findByIndicatorCodeAndRegionCodeAndObservedDate(
            String indicatorCode,
            MacroRegionCode regionCode,
            LocalDate observedDate
    );
}
