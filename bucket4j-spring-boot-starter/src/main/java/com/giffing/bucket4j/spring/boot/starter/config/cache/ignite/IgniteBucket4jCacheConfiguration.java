package com.giffing.bucket4j.spring.boot.starter.config.cache.ignite;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.config.condition.ConditionalOnAsynchronousPropertyCondition;
import com.giffing.bucket4j.spring.boot.starter.config.condition.ConditionalOnCache;
import com.giffing.bucket4j.spring.boot.starter.config.condition.ConditionalOnFilterConfigCacheEnabled;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import org.apache.ignite.Ignite;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnAsynchronousPropertyCondition
@ConditionalOnClass({ Ignite.class })
@ConditionalOnBean(Ignite.class)
@ConditionalOnCache("ignite")
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
	@ConditionalOnFilterConfigCacheEnabled
	public CacheManager<String, Bucket4JConfiguration> configCacheManager() {
		return new IgniteCacheManager<>(ignite.cache(configCacheName));
	}

	@Bean
	@ConditionalOnFilterConfigCacheEnabled
	public IgniteCacheListener<String, Bucket4JConfiguration> configCacheListener(ApplicationEventPublisher eventPublisher) {
		return new IgniteCacheListener<>(ignite.cache(configCacheName), eventPublisher);
	}
}
