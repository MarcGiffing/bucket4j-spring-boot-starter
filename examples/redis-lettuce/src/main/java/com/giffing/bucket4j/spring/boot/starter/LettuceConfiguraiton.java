package com.giffing.bucket4j.spring.boot.starter;

import io.lettuce.core.RedisClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LettuceConfiguraiton {

    @Bean
    public RedisClient redisClient(@Value("${spring.data.redis.port}") String port) {
        return RedisClient.create("redis://password@localhost:%s/".formatted(port));
    }

}
