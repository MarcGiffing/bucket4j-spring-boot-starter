package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.redisson;

import org.redisson.command.CommandExecutor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;

import io.github.bucket4j.redis.redisson.cas.RedissonBasedProxyManager;

@Configuration
@ConditionalOnClass(RedissonBasedProxyManager.class)
@ConditionalOnBean(CommandExecutor.class)
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "cache-to-use", havingValue = "redis-redisson", matchIfMissing = true)
public class RedissonBucket4jConfiguration {

	@Bean
	public SyncCacheResolver bucket4RedisResolver(CommandExecutor commandExecutor) {
		return new RedissonCacheResolver(commandExecutor);
	}
}
