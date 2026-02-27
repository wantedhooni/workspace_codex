package com.portal.admin.repo;

import com.portal.admin.domain.AdminUser;
import com.portal.admin.repo.search.FieldSearchableRepository;
import com.portal.admin.repo.search.SearchableRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long>, SearchableRepository<AdminUser>, FieldSearchableRepository<AdminUser> {
    Optional<AdminUser> findByUsername(String username);
}
