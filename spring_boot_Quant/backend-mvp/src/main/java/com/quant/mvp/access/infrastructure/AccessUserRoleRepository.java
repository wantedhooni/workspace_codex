package com.quant.mvp.access.infrastructure;

import com.quant.mvp.access.domain.AccessUserRole;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessUserRoleRepository extends JpaRepository<AccessUserRole, Long> {

    List<AccessUserRole> findByUserId(Long userId);

    List<AccessUserRole> findByUserIdIn(Collection<Long> userIds);

    List<AccessUserRole> findByRoleId(Long roleId);

    void deleteByUserId(Long userId);

    void deleteByUserIdIn(Collection<Long> userIds);

    boolean existsByRoleId(Long roleId);
}
