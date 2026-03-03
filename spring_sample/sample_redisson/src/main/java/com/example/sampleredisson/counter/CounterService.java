package com.example.sampleredisson.counter;

import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
public class CounterService {

    private static final String KEY_PREFIX = "sample:counter:";

    private final RedissonClient redissonClient;

    public CounterService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public CounterResponse get(String name) {
        return new CounterResponse(name, getAtomicLong(name).get());
    }

    public CounterResponse increment(String name, long delta) {
        long currentValue = getAtomicLong(name).addAndGet(delta);
        return new CounterResponse(name, currentValue);
    }

    private RAtomicLong getAtomicLong(String name) {
        return redissonClient.getAtomicLong(KEY_PREFIX + name);
    }
}
