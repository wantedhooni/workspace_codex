package com.quant.mvp.access.infrastructure;

import com.quant.mvp.access.domain.AccessPasswordHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessPasswordHistoryRepository extends JpaRepository<AccessPasswordHistory, Long> {

    List<AccessPasswordHistory> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);

    List<AccessPasswordHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    void deleteByUserId(Long userId);
}
