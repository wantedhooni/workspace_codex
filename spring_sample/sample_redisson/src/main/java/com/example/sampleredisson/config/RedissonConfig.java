package com.example.sampleredisson.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@EnableConfigurationProperties(RedissonProperties.class)
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(RedissonProperties properties) {
        Config config = new Config();
        SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress(properties.getAddress())
                .setConnectTimeout(properties.getConnectTimeout())
                .setTimeout(properties.getOperationTimeout());

        if (StringUtils.hasText(properties.getPassword())) {
            serverConfig.setPassword(properties.getPassword());
        }

        return Redisson.create(config);
    }
}
