package com.quant.mvp.access.application.mapper;

import com.quant.mvp.access.domain.AccessAccountSession;
import com.quant.mvp.pipeline.domain.AccountSession;

public final class AccessAccountSessionMapper {

    private AccessAccountSessionMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static AccountSession toDomain(AccessAccountSession session) {
        if (session == null) {
            return null;
        }
        return new AccountSession(
                session.getId(),
                session.getUserId(),
                session.getIpAddress(),
                session.getUserAgent(),
                session.isActive(),
                session.getCreatedAt(),
                session.getLastAccessAt()
        );
    }
}
