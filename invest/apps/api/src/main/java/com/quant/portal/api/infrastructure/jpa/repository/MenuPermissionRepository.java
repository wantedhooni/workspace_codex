package com.quant.portal.api.infrastructure.jpa.repository;

import com.quant.portal.api.infrastructure.jpa.repository.query.MenuPermissionQueryRepository;
import com.quant.portal.domain.admin.entity.MenuPermission;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuPermissionRepository extends JpaRepository<MenuPermission, Long>, MenuPermissionQueryRepository {

    Optional<MenuPermission> findByRoleCodeAndMenuKey(String roleCode, String menuKey);

    List<MenuPermission> findByRoleCodeIn(Collection<String> roleCodes);
}
