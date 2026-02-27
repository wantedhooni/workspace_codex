package com.quant.mvp.access.infrastructure;

import com.quant.mvp.access.domain.AccessPasswordResetToken;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessPasswordResetTokenRepository extends JpaRepository<AccessPasswordResetToken, Long> {

    Optional<AccessPasswordResetToken> findByTokenHash(String tokenHash);

    List<AccessPasswordResetToken> findByUserIdAndUsedAtIsNull(Long userId);

    void deleteByUserId(Long userId);

    void deleteByUserIdIn(Collection<Long> userIds);
}
