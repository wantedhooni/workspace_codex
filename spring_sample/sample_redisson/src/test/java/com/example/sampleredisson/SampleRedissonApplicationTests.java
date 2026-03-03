package com.example.sampleredisson;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.sampleredisson.counter.CounterService;
import com.example.sampleredisson.kv.KeyValueService;
import com.example.sampleredisson.lock.LockDemoService;
import com.example.sampleredisson.lock.LockExecutionRequest;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class SampleRedissonApplicationTests {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.4-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("app.redisson.address", () -> "redis://" + redis.getHost() + ":" + redis.getMappedPort(6379));
    }

    @Autowired
    private KeyValueService keyValueService;

    @Autowired
    private CounterService counterService;

    @Autowired
    private LockDemoService lockDemoService;

    @Autowired
    private RedissonClient redissonClient;

    @Test
    void keyValueOperationsWork() {
        keyValueService.save("trade:1001", "READY", 60L);

        var stored = keyValueService.find("trade:1001");

        assertThat(stored).isPresent();
        assertThat(stored.get().value()).isEqualTo("READY");
        assertThat(stored.get().ttlSeconds()).isNotNull();
    }

    @Test
    void counterIncrementsAtomically() {
        var result = counterService.increment("trade-sequence", 3L);

        assertThat(result.value()).isEqualTo(3L);
        assertThat(counterService.get("trade-sequence").value()).isEqualTo(3L);
    }

    @Test
    void lockProtectedSectionExecutesOncePerCall() {
        var result = lockDemoService.execute("settlement", new LockExecutionRequest(100L, 3_000L, 10L));

        assertThat(result.before()).isZero();
        assertThat(result.after()).isEqualTo(1L);
        assertThat(redissonClient.getAtomicLong("sample:lock-counter:settlement").get()).isEqualTo(1L);
    }
}
