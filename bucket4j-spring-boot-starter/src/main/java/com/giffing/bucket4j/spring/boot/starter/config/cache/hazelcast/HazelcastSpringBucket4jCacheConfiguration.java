package com.giffing.bucket4j.spring.boot.starter.config.cache.hazelcast;

import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.jcache.JCacheBucket4jConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;

/**
 * Configures the asynchronous support for Hazelcast. The synchronous support of Hazelcast
 * is already provided by the {@link JCacheBucket4jConfiguration}. It uses the {@link HazelcastInstance}
 * to access the {@link HazelcastInstance} to retrieve the cache.
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({ HazelcastCacheManager.class })
@ConditionalOnBean(HazelcastCacheManager.class)
@ConditionalOnMissingBean(SyncCacheResolver.class)
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "cache-to-use", havingValue = "hazelcast-spring", matchIfMissing = true)
public class HazelcastSpringBucket4jCacheConfiguration {
	
	private final HazelcastCacheManager hazelcastCacheManager;
	
	public HazelcastSpringBucket4jCacheConfiguration(HazelcastCacheManager hazelcastCacheManager) {
		this.hazelcastCacheManager = hazelcastCacheManager;
	}
	
	@Bean
	public AsyncCacheResolver hazelcastCacheResolver() {
		return new HazelcastCacheResolver(hazelcastCacheManager.getHazelcastInstance(), false);
	}
}
