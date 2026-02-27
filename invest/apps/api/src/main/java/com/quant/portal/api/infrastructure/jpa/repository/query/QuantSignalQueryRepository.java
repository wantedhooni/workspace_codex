package com.quant.portal.api.infrastructure.jpa.repository.query;

import com.quant.portal.api.application.query.QuantSignalSearchCondition;
import com.quant.portal.domain.quant.entity.QuantSignal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuantSignalQueryRepository {

    Page<QuantSignal> search(QuantSignalSearchCondition condition, Pageable pageable);
}
