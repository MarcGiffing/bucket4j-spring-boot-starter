package com.giffing.bucket4j.spring.boot.starter.config.cache.infinispan;

import org.apache.ignite.Ignite;
import org.infinispan.manager.CacheContainer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.hazelcast.core.HazelcastInstance;

@ConditionalOnClass({ CacheContainer.class })
@ConditionalOnBean(CacheContainer.class)
public class InfinispanBucket4jCacheConfiguration {
	
	private CacheContainer cacheContainer;
		
	public InfinispanBucket4jCacheConfiguration(CacheContainer cacheContainer) {
		this.cacheContainer = cacheContainer;
	}
	
	public AsyncCacheResolver infinispanCacheResolver() {
		return new InfinispanCacheResolver(cacheContainer);
	}
}
