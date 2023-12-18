package com.giffing.bucket4j.spring.boot.starter.config.cache.ignite;

import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;

import org.apache.ignite.Ignite;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass({ Ignite.class })
@ConditionalOnBean(Ignite.class)
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "cache-to-use", havingValue = "ignite", matchIfMissing = true)
public class IgniteBucket4jCacheConfiguration {
	
	private final Ignite ignite;
	private final String configCacheName;
	
	public IgniteBucket4jCacheConfiguration(Ignite ignite, Bucket4JBootProperties properties) {
		this.ignite = ignite;
		this.configCacheName = properties.getFilterConfigCacheName();
	}
	
	@Bean
	@ConditionalOnMissingBean(AsyncCacheResolver.class)
	public AsyncCacheResolver hazelcastCacheResolver() {
		return new IgniteCacheResolver(ignite);
	}

	@Bean
	@ConditionalOnMissingBean(CacheManager.class)
	@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "filter-config-caching-enabled", havingValue = "true", matchIfMissing = true)
	public CacheManager<String, Bucket4JConfiguration> configCacheManager() {
		return new IgniteCacheManager<>(ignite.cache(configCacheName));
	}

	@Bean
	@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "filter-config-caching-enabled", havingValue = "true", matchIfMissing = true)
	public IgniteCacheListener<String, Bucket4JConfiguration> configCacheListener() {
		return new IgniteCacheListener<>(ignite.cache(configCacheName));
	}
}
