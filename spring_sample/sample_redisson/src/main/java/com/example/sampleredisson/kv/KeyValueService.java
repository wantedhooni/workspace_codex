package com.example.sampleredisson.kv;

import java.time.Duration;
import java.util.Optional;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
public class KeyValueService {

    private static final String KEY_PREFIX = "sample:kv:";

    private final RedissonClient redissonClient;

    public KeyValueService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public KeyValueResponse save(String key, String value, Long ttlSeconds) {
        RBucket<String> bucket = redissonClient.getBucket(KEY_PREFIX + key);

        if (ttlSeconds == null) {
            bucket.set(value);
        } else {
            bucket.set(value, Duration.ofSeconds(ttlSeconds));
        }

        return toResponse(key, bucket);
    }

    public Optional<KeyValueResponse> find(String key) {
        RBucket<String> bucket = redissonClient.getBucket(KEY_PREFIX + key);

        if (!bucket.isExists()) {
            return Optional.empty();
        }

        return Optional.of(toResponse(key, bucket));
    }

    public boolean delete(String key) {
        return redissonClient.getBucket(KEY_PREFIX + key).delete();
    }

    private KeyValueResponse toResponse(String key, RBucket<String> bucket) {
        long ttlMillis = bucket.remainTimeToLive();
        Long ttlSeconds = ttlMillis > 0 ? ttlMillis / 1000 : null;
        return new KeyValueResponse(key, bucket.get(), ttlSeconds);
    }
}
