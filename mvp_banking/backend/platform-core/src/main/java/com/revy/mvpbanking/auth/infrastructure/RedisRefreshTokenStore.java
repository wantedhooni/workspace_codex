package com.revy.mvpbanking.auth.infrastructure;

import com.revy.mvpbanking.auth.domain.PrincipalType;
import com.revy.mvpbanking.auth.domain.RefreshTokenRecord;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private final StringRedisTemplate redisTemplate;

    public RedisRefreshTokenStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(String refreshToken, RefreshTokenRecord record, long ttlSeconds) {
        String value = String.join("::",
                record.tokenId(),
                record.principalId().toString(),
                record.email(),
                record.displayName(),
                record.principalType().name(),
                String.join(",", record.roles()),
                String.valueOf(record.expiresAt().getEpochSecond()));

        redisTemplate.opsForValue().set(key(refreshToken), value, ttlSeconds, TimeUnit.SECONDS);
    }

    @Override
    public Optional<RefreshTokenRecord> find(String refreshToken) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(refreshToken)))
                .map(this::deserialize);
    }

    @Override
    public void delete(String refreshToken) {
        redisTemplate.delete(key(refreshToken));
    }

    private String key(String refreshToken) {
        return "auth:refresh:" + refreshToken;
    }

    private RefreshTokenRecord deserialize(String raw) {
        String[] parts = raw.split("::", -1);
        List<String> roles = parts[5].isBlank() ? List.of() : Arrays.stream(parts[5].split(",")).toList();

        return new RefreshTokenRecord(
                parts[0],
                UUID.fromString(parts[1]),
                parts[2],
                parts[3],
                PrincipalType.valueOf(parts[4]),
                roles,
                Instant.ofEpochSecond(Long.parseLong(parts[6]))
        );
    }
}
