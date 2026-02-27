package com.portal.admin.repo;

import com.portal.admin.domain.Permission;
import com.portal.admin.repo.search.FieldSearchableRepository;
import com.portal.admin.repo.search.SearchableRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long>, SearchableRepository<Permission>, FieldSearchableRepository<Permission> {
    Optional<Permission> findByCode(String code);
}
