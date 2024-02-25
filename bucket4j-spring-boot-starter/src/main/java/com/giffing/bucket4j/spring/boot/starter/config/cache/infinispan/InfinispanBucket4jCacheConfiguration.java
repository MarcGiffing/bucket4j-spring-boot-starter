package com.giffing.bucket4j.spring.boot.starter.config.cache.infinispan;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import org.infinispan.manager.CacheContainer;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass({CacheContainer.class})
@ConditionalOnBean(CacheContainer.class)
@ConditionalOnMissingBean(AsyncCacheResolver.class)
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "cache-to-use", havingValue = "infinispan", matchIfMissing = true)
public class InfinispanBucket4jCacheConfiguration {

	private final CacheContainer cacheContainer;
	private final String configCacheName;

	public InfinispanBucket4jCacheConfiguration(CacheContainer cacheContainer, Bucket4JBootProperties properties) {
		this.cacheContainer = cacheContainer;
		this.configCacheName = properties.getFilterConfigCacheName();
	}

	@Bean
	public AsyncCacheResolver infinispanCacheResolver() {
		return new InfinispanCacheResolver(cacheContainer);
	}

	@Bean
	@ConditionalOnMissingBean(CacheManager.class)
	@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "filter-config-caching-enabled", havingValue = "true")
	public CacheManager<String, Bucket4JConfiguration> configCacheManager() {
		return new InfinispanCacheManager<>(cacheContainer.getCache(configCacheName));
	}

	@Bean
	@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "filter-config-caching-enabled", havingValue = "true")
	public InfinispanCacheListener<String, Bucket4JConfiguration> configCacheListener(ApplicationEventPublisher eventPublisher) {
		return new InfinispanCacheListener<>(cacheContainer.getCache(configCacheName), eventPublisher);
	}
}
