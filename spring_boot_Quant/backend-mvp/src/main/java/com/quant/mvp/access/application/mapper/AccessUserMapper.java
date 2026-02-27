package com.quant.mvp.access.application.mapper;

import com.quant.mvp.access.domain.AccessUser;
import com.quant.mvp.pipeline.domain.UserAccount;

public final class AccessUserMapper {

    private AccessUserMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static UserAccount toDomain(AccessUser user) {
        if (user == null) {
            return null;
        }
        return new UserAccount(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getStatus(),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
