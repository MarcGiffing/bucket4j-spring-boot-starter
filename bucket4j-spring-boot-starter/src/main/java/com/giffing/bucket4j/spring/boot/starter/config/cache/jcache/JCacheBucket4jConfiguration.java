package com.giffing.bucket4j.spring.boot.starter.config.cache.jcache;

import javax.cache.CacheManager;
import javax.cache.Caching;

import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Caching.class, JCacheCacheManager.class})
@ConditionalOnBean(CacheManager.class)
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "cache-to-use", havingValue = "jcache", matchIfMissing = true)
public class JCacheBucket4jConfiguration {

	private final CacheManager cacheManager;
	private final String configCacheName;

	public JCacheBucket4jConfiguration(CacheManager cacheManager, Bucket4JBootProperties properties) {
		this.cacheManager = cacheManager;
		this.configCacheName = properties.getFilterConfigCacheName();
	}

	@Bean
	@ConditionalOnMissingBean(SyncCacheResolver.class)
	public SyncCacheResolver bucket4jCacheResolver() {
		return new JCacheCacheResolver(cacheManager);
	}

	@Bean
	@ConditionalOnMissingBean(com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager.class)
	@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "filter-config-caching-enabled", havingValue = "true", matchIfMissing = true)
	public com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager<String, Bucket4JConfiguration> configCacheManager(
	) {
		return new com.giffing.bucket4j.spring.boot.starter.config.cache.jcache.JCacheCacheManager<>(cacheManager.getCache(configCacheName));
	}

	@Bean
	@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "filter-config-caching-enabled", havingValue = "true", matchIfMissing = true)
	public JCacheCacheListener<String, Bucket4JConfiguration> configCacheListener() {
		return new JCacheCacheListener<>(cacheManager.getCache(configCacheName));
	}

}
