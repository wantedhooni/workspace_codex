package com.quant.mvp.access.infrastructure;

import com.quant.mvp.access.domain.AccessMenuPermission;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessMenuPermissionRepository extends JpaRepository<AccessMenuPermission, Long>, AccessMenuPermissionQueryRepository {

    Optional<AccessMenuPermission> findByMenuIdAndRoleId(Long menuId, Long roleId);

    List<AccessMenuPermission> findByRoleIdIn(Collection<Long> roleIds);

    List<AccessMenuPermission> findByMenuIdAndRoleIdIn(Long menuId, Collection<Long> roleIds);

    void deleteByRoleId(Long roleId);

    void deleteByMenuId(Long menuId);
}
