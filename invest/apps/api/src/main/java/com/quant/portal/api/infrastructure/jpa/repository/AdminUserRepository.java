package com.quant.portal.api.infrastructure.jpa.repository;

import com.quant.portal.api.infrastructure.jpa.repository.query.AdminUserQueryRepository;
import com.quant.portal.domain.admin.entity.AdminUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long>, AdminUserQueryRepository {

    Optional<AdminUser> findByUsername(String username);
}

