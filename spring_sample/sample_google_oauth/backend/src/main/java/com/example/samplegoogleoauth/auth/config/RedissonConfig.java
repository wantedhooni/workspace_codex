package com.example.samplegoogleoauth.auth.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Redis 기반 인증 상태 관리를 위한 Redisson 클라이언트를 구성한다.
 */
@Configuration
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    RedissonClient redissonClient(RedissonProperties properties) {
        Config config = new Config();
        SingleServerConfig serverConfig = config.useSingleServer()
            .setAddress(properties.address())
            .setConnectTimeout(properties.connectTimeout())
            .setTimeout(properties.operationTimeout());

        if (StringUtils.hasText(properties.password())) {
            serverConfig.setPassword(properties.password());
        }

        return Redisson.create(config);
    }
}
