package com.quant.portal.api.infrastructure.jpa.repository.query;

import com.quant.portal.api.application.query.MenuPermissionSearchCondition;
import com.quant.portal.domain.admin.entity.MenuPermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MenuPermissionQueryRepository {

    Page<MenuPermission> search(MenuPermissionSearchCondition condition, Pageable pageable);
}
