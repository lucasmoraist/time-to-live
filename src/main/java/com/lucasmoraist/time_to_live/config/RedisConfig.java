package com.lucasmoraist.time_to_live.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    @Bean
    public RedissonClient redissonClient() {
        Config redissonConfig = new Config();
        redissonConfig.useSingleServer()
                .setAddress("redis://localhost:6379")
                .setConnectionPoolSize(128)
                .setConnectionMinimumIdleSize(50);

        return Redisson.create(redissonConfig);
    }

}
