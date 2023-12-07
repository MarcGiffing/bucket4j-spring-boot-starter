package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.lettuce;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(LettuceBasedProxyManager.class)
@ConditionalOnBean(RedisClient.class)
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "cache-to-use", havingValue = "redis-lettuce", matchIfMissing = true)
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
	@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "filter-config-caching-enabled", havingValue = "true", matchIfMissing = true)
	public CacheManager<String, Bucket4JConfiguration> configCacheManager() {
		return new LettuceCacheManager<>(redisClient, configCacheName, Bucket4JConfiguration.class);
	}

	@Bean
	@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "filter-config-caching-enabled", havingValue = "true", matchIfMissing = true)
	public LettuceCacheListener<String, Bucket4JConfiguration> configCacheListener() {
		return new LettuceCacheListener<>(redisClient, configCacheName, String.class, Bucket4JConfiguration.class);
	}
}
