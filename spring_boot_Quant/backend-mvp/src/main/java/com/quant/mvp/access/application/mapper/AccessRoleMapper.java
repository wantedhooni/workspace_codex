package com.quant.mvp.access.application.mapper;

import com.quant.mvp.access.domain.AccessRole;
import com.quant.mvp.pipeline.domain.Role;

public final class AccessRoleMapper {

    private AccessRoleMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static Role toDomain(AccessRole role) {
        if (role == null) {
            return null;
        }
        return new Role(
                role.getId(),
                role.getRoleCode(),
                role.getRoleName(),
                role.getDescription(),
                role.isSystemRole(),
                role.getCreatedAt()
        );
    }
}
