package com.giffing.bucket4j.spring.boot.starter;

import org.redisson.Redisson;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfiguraiton {

    @Bean
	CommandAsyncExecutor getCommandAsyncExecutor(@Value("${spring.data.redis.host}") String host,
                                  @Value("${spring.data.redis.port}") String port) {
        var address = "redis://%s:%s".formatted(host, port);

        var config = new Config();
        config.useSingleServer()
                .setAddress(address)
                .setRetryAttempts(5);
		return ((Redisson)Redisson.create(config)).getCommandExecutor();
    }

}
