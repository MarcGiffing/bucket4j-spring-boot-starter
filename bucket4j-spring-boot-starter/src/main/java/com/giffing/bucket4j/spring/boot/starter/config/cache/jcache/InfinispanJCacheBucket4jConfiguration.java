package com.giffing.bucket4j.spring.boot.starter.config.cache.jcache;

import javax.cache.Caching;

import org.infinispan.manager.CacheContainer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.infinispan.InfinispanCacheResolver;

/**
 * The configuration class for Infinispan. Infinispan is not directly supported by
 * bucket4j. See {@link InfinispanCacheResolver} for more informations.
 */
@Configuration
@ConditionalOnClass({ CacheContainer.class, Caching.class, JCacheCacheManager.class })
@ConditionalOnBean(CacheContainer.class)
public class InfinispanJCacheBucket4jConfiguration {

	private CacheContainer cacheContainer;
	
	public InfinispanJCacheBucket4jConfiguration(CacheContainer cacheContainer) {
		this.cacheContainer = cacheContainer;
	}
	
	@Bean
	public SyncCacheResolver bucket4jCacheResolver() {
		return new InfinispanJCacheCacheResolver(cacheContainer);
	}
	
}
