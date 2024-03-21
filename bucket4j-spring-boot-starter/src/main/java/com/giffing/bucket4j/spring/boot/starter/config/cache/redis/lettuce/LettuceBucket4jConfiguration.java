package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.lettuce;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.config.condition.ConditionalOnAsynchronousPropertyCondition;
import com.giffing.bucket4j.spring.boot.starter.config.condition.ConditionalOnCache;
import com.giffing.bucket4j.spring.boot.starter.config.condition.ConditionalOnFilterConfigCacheEnabled;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnAsynchronousPropertyCondition
@ConditionalOnClass(LettuceBasedProxyManager.class)
@ConditionalOnBean(RedisClient.class)
@ConditionalOnCache("redis-lettuce")
public class LettuceBucket4jConfiguration {

	private final RedisClient redisClient;
	private final String configCacheName;
	public LettuceBucket4jConfiguration(RedisClient redisClient, Bucket4JBootProperties properties){
		this.redisClient = redisClient;
		this.configCacheName = properties.getFilterConfigCacheName();
	}

	@Bean
	@ConditionalOnMissingBean(AsyncCacheResolver.class)
	public AsyncCacheResolver bucket4RedisResolver() {
		return new LettuceCacheResolver(this.redisClient);
	}

	@Bean
	@ConditionalOnMissingBean(CacheManager.class)
	@ConditionalOnFilterConfigCacheEnabled
	public CacheManager<String, Bucket4JConfiguration> configCacheManager() {
		return new LettuceCacheManager<>(redisClient, configCacheName, Bucket4JConfiguration.class);
	}

	@Bean
	@ConditionalOnFilterConfigCacheEnabled
	public LettuceCacheListener<String, Bucket4JConfiguration> configCacheListener(ApplicationEventPublisher eventPublisher) {
		return new LettuceCacheListener<>(redisClient, configCacheName, String.class, Bucket4JConfiguration.class, eventPublisher);
	}
}
