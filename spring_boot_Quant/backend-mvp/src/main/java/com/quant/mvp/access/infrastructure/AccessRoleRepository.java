package com.quant.mvp.access.infrastructure;

import com.quant.mvp.access.domain.AccessRole;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessRoleRepository extends JpaRepository<AccessRole, Long>, AccessRoleQueryRepository {

    Optional<AccessRole> findByRoleCodeIgnoreCase(String roleCode);

    boolean existsByRoleCodeIgnoreCase(String roleCode);
}
