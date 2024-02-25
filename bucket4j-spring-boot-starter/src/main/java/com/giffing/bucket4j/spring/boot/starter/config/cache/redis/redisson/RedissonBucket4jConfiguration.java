package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.redisson;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import io.github.bucket4j.redis.redisson.cas.RedissonBasedProxyManager;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(RedissonBasedProxyManager.class)
@ConditionalOnBean(CommandAsyncExecutor.class)
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "cache-to-use", havingValue = "redis-redisson", matchIfMissing = true)
public class RedissonBucket4jConfiguration {

	private final CommandAsyncExecutor commandExecutor;
	private final RedissonClient redissonClient;
	private final String configCacheName;

	public RedissonBucket4jConfiguration(CommandAsyncExecutor commandExecutor, Bucket4JBootProperties properties) {
		this.commandExecutor = commandExecutor;
		this.configCacheName = properties.getFilterConfigCacheName();

		Config config = new Config(commandExecutor.getServiceManager().getCfg());
		config.useSingleServer()
				.setConnectionMinimumIdleSize(1)
				.setConnectionPoolSize(2);
		this.redissonClient = Redisson.create(config);
	}

	@Bean
	@ConditionalOnMissingBean(AsyncCacheResolver.class)
	public AsyncCacheResolver bucket4RedisResolver() {
		return new RedissonCacheResolver(commandExecutor);
	}

	@Bean
	@ConditionalOnMissingBean(CacheManager.class)
	@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "filter-config-caching-enabled", havingValue = "true")
	public CacheManager<String, Bucket4JConfiguration> configCacheManager() {
		return new RedissonCacheManager<>(this.redissonClient, configCacheName);
	}

	@Bean
	@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "filter-config-caching-enabled", havingValue = "true")
	public RedissonCacheListener<String, Bucket4JConfiguration> configCacheListener(ApplicationEventPublisher eventPublisher) {
		return new RedissonCacheListener<>(this.redissonClient, configCacheName, eventPublisher);
	}
}
