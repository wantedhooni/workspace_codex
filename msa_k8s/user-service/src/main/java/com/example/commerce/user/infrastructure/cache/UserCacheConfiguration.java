package com.example.commerce.user.infrastructure.cache;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.commerce.user.domain.User;

/**
 * 회원 조회 Redis 캐시의 TTL, 직렬화, 장애 격리 정책을 구성한다.
 */
@Configuration
public class UserCacheConfiguration implements CachingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(UserCacheConfiguration.class);

    /**
     * 회원 Redis 캐시 구성을 생성한다.
     */
    public UserCacheConfiguration() {
    }

    /**
     * 회원 캐시를 JSON으로 저장하고 10분 후 만료시키는 캐시 관리자를 제공한다.
     *
     * @param connectionFactory Redis 연결 팩토리
     * @param objectMapper 애플리케이션 JSON 매퍼
     * @return Redis 캐시 관리자
     */
    @Bean
    RedisCacheManager redisCacheManager(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper) {
        Jackson2JsonRedisSerializer<User> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper.copy(), User.class);
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer));
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(configuration)
                .build();
    }

    /**
     * Redis 장애가 회원 API 장애로 전파되지 않도록 캐시 오류를 기록하고 원본 DB 조회를 허용한다.
     *
     * @return 장애 격리형 캐시 오류 처리기
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Redis 캐시 조회에 실패했습니다. cache={}, key={}", cache.getName(), key, exception);
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Redis 캐시 저장에 실패했습니다. cache={}, key={}", cache.getName(), key, exception);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Redis 캐시 삭제에 실패했습니다. cache={}, key={}", cache.getName(), key, exception);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Redis 캐시 초기화에 실패했습니다. cache={}", cache.getName(), exception);
            }
        };
    }
}
