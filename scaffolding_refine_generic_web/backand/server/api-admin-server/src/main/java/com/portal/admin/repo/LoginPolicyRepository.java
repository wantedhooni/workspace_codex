package com.portal.admin.repo;

import com.portal.admin.domain.LoginPolicy;
import com.portal.admin.repo.search.FieldSearchableRepository;
import com.portal.admin.repo.search.SearchableRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginPolicyRepository extends JpaRepository<LoginPolicy, Long>, SearchableRepository<LoginPolicy>, FieldSearchableRepository<LoginPolicy> {
    Optional<LoginPolicy> findFirstByName(String name);
}
