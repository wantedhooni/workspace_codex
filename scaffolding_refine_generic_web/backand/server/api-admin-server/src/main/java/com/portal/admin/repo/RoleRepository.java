package com.portal.admin.repo;

import com.portal.admin.domain.Role;
import com.portal.admin.repo.search.FieldSearchableRepository;
import com.portal.admin.repo.search.SearchableRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long>, SearchableRepository<Role>, FieldSearchableRepository<Role> {
    Optional<Role> findByName(String name);
}
