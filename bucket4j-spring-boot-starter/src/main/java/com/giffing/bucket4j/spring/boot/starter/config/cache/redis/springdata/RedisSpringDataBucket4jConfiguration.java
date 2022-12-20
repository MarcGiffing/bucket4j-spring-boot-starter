package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.springdata;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisCommands;

import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;

import io.github.bucket4j.redis.spring.cas.SpringDataRedisBasedProxyManager;

@Configuration
@ConditionalOnClass(SpringDataRedisBasedProxyManager.class)
@ConditionalOnBean(RedisCommands.class)
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "cache-to-use", havingValue = "redis-springdata", matchIfMissing = true)
public class RedisSpringDataBucket4jConfiguration {

	@Bean
	public SyncCacheResolver bucket4RedisResolver(RedisCommands redisCommands) {
		return new RedisSpringDataCacheResolver(redisCommands);
	}
}
