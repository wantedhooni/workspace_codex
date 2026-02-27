package com.quant.mvp.access.infrastructure;

import com.quant.mvp.access.domain.AccessRole;
import java.util.List;

public interface AccessRoleQueryRepository {

    List<AccessRole> searchRoles(Long roleId, String roleCode, String roleName);
}
