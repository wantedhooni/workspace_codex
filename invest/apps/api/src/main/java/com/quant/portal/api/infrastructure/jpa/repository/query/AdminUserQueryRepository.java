package com.quant.portal.api.infrastructure.jpa.repository.query;

import com.quant.portal.api.application.query.AdminUserSearchCondition;
import com.quant.portal.domain.admin.entity.AdminUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserQueryRepository {

    Page<AdminUser> search(AdminUserSearchCondition condition, Pageable pageable);

    long countByRoleCode(String roleCode);
}

