package com.example.sampleredisson.lock;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LockDemoService {

    private static final String LOCK_PREFIX = "sample:lock:";
    private static final String COUNTER_PREFIX = "sample:lock-counter:";

    private final RedissonClient redissonClient;

    public LockDemoService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public LockExecutionResponse execute(String name, LockExecutionRequest request) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + name);
        RAtomicLong counter = redissonClient.getAtomicLong(COUNTER_PREFIX + name);

        boolean acquired;
        try {
            acquired = lock.tryLock(request.waitMillis(), request.leaseMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("락 획득 대기 중 인터럽트가 발생했습니다.", exception);
        }

        if (!acquired) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "락을 획득하지 못했습니다.");
        }

        try {
            long before = counter.get();
            Thread.sleep(request.processingMillis());
            long after = counter.incrementAndGet();
            return new LockExecutionResponse(name, before, after, Instant.now());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("락 보호 작업 중 인터럽트가 발생했습니다.", exception);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
