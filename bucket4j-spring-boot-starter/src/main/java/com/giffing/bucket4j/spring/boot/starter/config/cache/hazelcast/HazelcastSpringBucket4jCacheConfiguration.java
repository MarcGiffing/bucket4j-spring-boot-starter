package com.giffing.bucket4j.spring.boot.starter.config.cache.hazelcast;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.jcache.JCacheBucket4jConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.condition.ConditionalOnCache;
import com.giffing.bucket4j.spring.boot.starter.config.condition.ConditionalOnFilterConfigCacheEnabled;
import com.giffing.bucket4j.spring.boot.starter.config.condition.ConditionalOnSynchronousPropertyCondition;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the asynchronous support for Hazelcast. The synchronous support of Hazelcast
 * is already provided by the {@link JCacheBucket4jConfiguration}. It uses the {@link HazelcastInstance}
 * to access the {@link HazelcastInstance} to retrieve the cache.
 */
@Configuration
@ConditionalOnSynchronousPropertyCondition
@ConditionalOnClass({HazelcastCacheManager.class})
@ConditionalOnBean(HazelcastCacheManager.class)
@ConditionalOnCache("hazelcast-spring")
public class HazelcastSpringBucket4jCacheConfiguration {

	private final HazelcastInstance hazelcastInstance;
	private final String configCacheName;

	public HazelcastSpringBucket4jCacheConfiguration(HazelcastCacheManager hazelcastCacheManager, Bucket4JBootProperties properties) {
		this.hazelcastInstance = hazelcastCacheManager.getHazelcastInstance();
		this.configCacheName = properties.getFilterConfigCacheName();
	}

	@Bean
	@ConditionalOnMissingBean(SyncCacheResolver.class)
	public SyncCacheResolver hazelcastCacheResolver() {
		return new HazelcastCacheResolver(hazelcastInstance, false);
	}

	@Bean
	@ConditionalOnMissingBean(CacheManager.class)
	@ConditionalOnFilterConfigCacheEnabled
	public CacheManager<String, Bucket4JConfiguration> configCacheManager() {
		IMap<String, Bucket4JConfiguration> map = hazelcastInstance.getMap(configCacheName);
		return new com.giffing.bucket4j.spring.boot.starter.config.cache.hazelcast.HazelcastCacheManager<>(map);
	}

	@Bean
	@ConditionalOnFilterConfigCacheEnabled
	public HazelcastCacheListener<String, Bucket4JConfiguration> configCacheListener(ApplicationEventPublisher eventPublisher) {
		IMap<String, Bucket4JConfiguration> map = hazelcastInstance.getMap(configCacheName);
		return new HazelcastCacheListener<>(map, eventPublisher);
	}

}
