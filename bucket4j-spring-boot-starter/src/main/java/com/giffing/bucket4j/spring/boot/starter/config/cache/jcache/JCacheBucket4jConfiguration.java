package com.giffing.bucket4j.spring.boot.starter.config.cache.jcache;

import javax.cache.CacheManager;
import javax.cache.Caching;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;

@Configuration
@ConditionalOnClass({ Caching.class, JCacheCacheManager.class })
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
