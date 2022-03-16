package com.giffing.bucket4j.spring.boot.starter.config.cache.hazelcast;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.jcache.JCacheBucket4jConfiguration;
import com.hazelcast.core.HazelcastInstance;

/**
 * Configures the asynchronous support for Hazelcast. The synchronous support of Hazelcast
 * is already provided by the {@link JCacheBucket4jConfiguration}. It uses the {@link HazelcastInstance}
 * to access the {@link HazelcastInstance} to retrieve the cache.
 */
@Configuration
@ConditionalOnClass({ HazelcastInstance.class })
@ConditionalOnBean(HazelcastInstance.class)
@ConditionalOnMissingBean(AsyncCacheResolver.class)
public class HazelcastBucket4jCacheConfiguration {
	
	private HazelcastInstance hazelcastInstance;
	
	public HazelcastBucket4jCacheConfiguration(HazelcastInstance hazelcastInstance) {
		this.hazelcastInstance = hazelcastInstance;
	}
	
	@Bean
	public AsyncCacheResolver hazelcastCacheResolver() {
		return new HazelcastCacheResolver(hazelcastInstance);
	}
}
