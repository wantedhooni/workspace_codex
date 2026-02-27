package com.derivops.mvp.cashfx;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FxRequestRepository extends JpaRepository<FxRequest, UUID>, JpaSpecificationExecutor<FxRequest> {
}
