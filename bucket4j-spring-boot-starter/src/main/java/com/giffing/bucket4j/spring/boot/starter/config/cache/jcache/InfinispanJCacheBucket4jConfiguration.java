package com.giffing.bucket4j.spring.boot.starter.config.cache.jcache;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.infinispan.InfinispanCacheListener;
import com.giffing.bucket4j.spring.boot.starter.config.cache.infinispan.InfinispanCacheManager;
import com.giffing.bucket4j.spring.boot.starter.config.cache.infinispan.InfinispanCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;

import org.infinispan.manager.CacheContainer;

import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.Caching;

/**
 * The configuration class for Infinispan. Infinispan is not directly supported by
 * bucket4j. See {@link InfinispanCacheResolver} for more informations.
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({CacheContainer.class, Caching.class, JCacheCacheManager.class})
@ConditionalOnBean(CacheContainer.class)
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "cache-to-use", havingValue = "jcache-ignite", matchIfMissing = true)
public class InfinispanJCacheBucket4jConfiguration {

	private final CacheContainer cacheContainer;
	private final String configCacheName;

	public InfinispanJCacheBucket4jConfiguration(CacheContainer cacheContainer, Bucket4JBootProperties properties) {
		this.cacheContainer = cacheContainer;
		this.configCacheName = properties.getFilterConfigCacheName();
	}

	@Bean
	@ConditionalOnMissingBean(SyncCacheResolver.class)
	public SyncCacheResolver bucket4jCacheResolver() {
		return new InfinispanJCacheCacheResolver(cacheContainer);
	}

	@Bean
	@ConditionalOnMissingBean(CacheManager.class)
	@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "filter-config-caching-enabled", havingValue = "true", matchIfMissing = true)
	public CacheManager<String, Bucket4JConfiguration> configCacheManager() {
		return new InfinispanCacheManager<>(cacheContainer.getCache(configCacheName));
	}

	@Bean
	@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "filter-config-caching-enabled", havingValue = "true", matchIfMissing = true)
	public InfinispanCacheListener<String, Bucket4JConfiguration> configCacheListener() {
		return new InfinispanCacheListener<>(cacheContainer.getCache(configCacheName));
	}
}
