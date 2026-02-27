package com.quant.mvp.access.infrastructure;

import com.quant.mvp.access.domain.AccessAccountSession;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessAccountSessionRepository extends JpaRepository<AccessAccountSession, Long> {

    List<AccessAccountSession> findByUserIdOrderByIdAsc(Long userId);

    List<AccessAccountSession> findByUserIdAndActiveTrueOrderByLastAccessAtDescIdAsc(Long userId);

    void deleteByUserId(Long userId);

    void deleteByUserIdIn(Collection<Long> userIds);
}
