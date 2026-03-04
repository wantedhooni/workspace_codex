package com.revy.mvpbanking.auth.infrastructure;

import com.revy.mvpbanking.auth.domain.RefreshTokenRecord;
import java.util.Optional;

public interface RefreshTokenStore {
    void save(String refreshToken, RefreshTokenRecord record, long ttlSeconds);
    Optional<RefreshTokenRecord> find(String refreshToken);
    void delete(String refreshToken);
}
