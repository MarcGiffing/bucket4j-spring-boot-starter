package com.giffing.bucket4j.spring.boot.starter.config.cache.infinispan;

import org.infinispan.manager.CacheContainer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;

@Configuration
@ConditionalOnClass({ CacheContainer.class })
@ConditionalOnBean(CacheContainer.class)
@ConditionalOnMissingBean(AsyncCacheResolver.class)
public class InfinispanBucket4jCacheConfiguration {
	
	private CacheContainer cacheContainer;
		
	public InfinispanBucket4jCacheConfiguration(CacheContainer cacheContainer) {
		this.cacheContainer = cacheContainer;
	}
	
	@Bean
	public AsyncCacheResolver infinispanCacheResolver() {
		return new InfinispanCacheResolver(cacheContainer);
	}
}
