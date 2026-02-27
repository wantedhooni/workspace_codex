package com.quant.portal.api.infrastructure.jpa.repository.query;

import com.quant.portal.api.application.query.MacroIndicatorSearchCondition;
import com.quant.portal.domain.macro.entity.MacroIndicator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MacroIndicatorQueryRepository {

    Page<MacroIndicator> search(MacroIndicatorSearchCondition condition, Pageable pageable);
}
