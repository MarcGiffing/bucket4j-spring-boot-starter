package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.lettuce;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;

import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;

@Configuration
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnClass(LettuceBasedProxyManager.class)
@ConditionalOnBean(RedisClient.class)
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "cache-to-use", havingValue = "redis-lettuce", matchIfMissing = true)
public class LettuceBucket4jConfiguration {

	@Bean
	public AsyncCacheResolver bucket4RedisResolver(RedisClient redisClient) {
		return new LettuceCacheResolver(redisClient);
	}
}
