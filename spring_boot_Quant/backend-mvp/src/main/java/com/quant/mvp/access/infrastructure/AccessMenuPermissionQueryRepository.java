package com.quant.mvp.access.infrastructure;

import com.quant.mvp.access.domain.AccessMenuPermission;
import java.util.List;

public interface AccessMenuPermissionQueryRepository {

    List<AccessMenuPermission> searchMenuPermissions(Long roleId, Long menuId, String roleCode, String menuKey);
}
