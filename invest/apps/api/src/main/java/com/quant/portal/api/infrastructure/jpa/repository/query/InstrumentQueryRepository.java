package com.quant.portal.api.infrastructure.jpa.repository.query;

import com.quant.portal.api.application.query.InstrumentSearchCondition;
import com.quant.portal.domain.portfolio.entity.Instrument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InstrumentQueryRepository {

    Page<Instrument> search(InstrumentSearchCondition condition, Pageable pageable);
}
