package com.portal.admin.repo;

import com.portal.admin.domain.RoleRouteMapping;
import com.portal.admin.repo.search.FieldSearchableRepository;
import com.portal.admin.repo.search.SearchableRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRouteMappingRepository extends JpaRepository<RoleRouteMapping, Long>, SearchableRepository<RoleRouteMapping>, FieldSearchableRepository<RoleRouteMapping> {
    Optional<RoleRouteMapping> findFirstByRoleIdAndPatternAndHttpMethod(Long roleId, String pattern, String httpMethod);
}
