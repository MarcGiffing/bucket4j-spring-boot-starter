package com.giffing.bucket4j.spring.boot.starter.config.cache.jcache;

import javax.cache.CacheManager;
import javax.cache.Caching;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;

@Configuration
@ConditionalOnClass({ Caching.class, JCacheCacheManager.class })
@ConditionalOnBean(CacheManager.class)
@ConditionalOnMissingBean(SyncCacheResolver.class)
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "cache-to-use", havingValue = "jcache", matchIfMissing = true)
public class JCacheBucket4jConfiguration {
	private CacheManager cacheManager;
	
	public JCacheBucket4jConfiguration(CacheManager cacheManager){
		this.cacheManager = cacheManager;
	}

	@Bean
	public SyncCacheResolver bucket4jCacheResolver() {
		return new JCacheCacheResolver(cacheManager);
	}
	
}
