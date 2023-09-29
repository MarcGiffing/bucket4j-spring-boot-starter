package com.giffing.bucket4j.spring.boot.starter;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.redisson.connection.ServiceManager;
import org.redisson.connection.SingleConnectionManager;
import org.redisson.liveobject.core.RedissonObjectBuilder;
import org.redisson.rx.CommandRxService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfiguraiton {

    @Bean
    public CommandRxService commandReactiveService(
            RedissionConfig redissionConfig) {
        return new CommandRxService(
                new SingleConnectionManager(
                        redissionConfig.ssc(),
                        new ServiceManager(redissionConfig.client().getConfig())),
                new RedissonObjectBuilder(redissionConfig.client()));

    }

    private record RedissionConfig (RedissonClient client, SingleServerConfig ssc) {}

    @Bean
    RedissionConfig redissonConfig(@Value("${spring.redis.host}") String host,
                                  @Value("${spring.redis.port}") String port) {
        var address = "redis://%s:%s".formatted(host, port);

        var config = new Config();
        var singleServerConfig = config.useSingleServer()
                .setAddress(address)
                .setRetryAttempts(5);
        return new RedissionConfig(Redisson.create(config), singleServerConfig);
    }

}
