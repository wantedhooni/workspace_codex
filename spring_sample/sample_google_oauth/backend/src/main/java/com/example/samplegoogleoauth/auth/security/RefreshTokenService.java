package com.example.samplegoogleoauth.auth.security;

import java.time.Duration;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

/**
 * Redis에 refresh token을 저장하고 회수한다.
 */
@Service
public class RefreshTokenService {

    private static final String KEY_PREFIX = "sample_google_oauth:refresh:";

    private final RedissonClient redissonClient;

    public RefreshTokenService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 회원의 최신 refresh token을 Redis에 저장한다.
     */
    public void save(Long memberId, String refreshToken, Duration ttl) {
        RBucket<String> bucket = redissonClient.getBucket(KEY_PREFIX + memberId);
        bucket.set(refreshToken, ttl);
    }

    /**
     * Redis에 저장된 refresh token과 현재 요청 토큰이 일치하는지 검증한다.
     */
    public boolean matches(Long memberId, String refreshToken) {
        String savedToken = redissonClient.<String>getBucket(KEY_PREFIX + memberId).get();
        return refreshToken.equals(savedToken);
    }

    /**
     * 로그아웃 또는 재발급 시 기존 refresh token을 제거한다.
     */
    public void revoke(Long memberId) {
        redissonClient.<String>getBucket(KEY_PREFIX + memberId).delete();
    }
}
