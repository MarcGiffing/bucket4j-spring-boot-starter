package com.giffing.bucket4j.spring.boot.starter.config.cache.hazelcast;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.config.cache.jcache.JCacheBucket4jConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the asynchronous support for Hazelcast. The synchronous support of Hazelcast
 * is already provided by the {@link JCacheBucket4jConfiguration}. It uses the {@link HazelcastInstance}
 * to access the {@link HazelcastInstance} to retrieve the cache.
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass({HazelcastInstance.class})
@ConditionalOnBean(HazelcastInstance.class)
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "cache-to-use", havingValue = "hazelcast-reactive", matchIfMissing = true)
public class HazelcastReactiveBucket4jCacheConfiguration {

	private final HazelcastInstance hazelcastInstance;

	private final String configCacheName;

	public HazelcastReactiveBucket4jCacheConfiguration(HazelcastInstance hazelcastInstance, Bucket4JBootProperties properties) {
		this.hazelcastInstance = hazelcastInstance;
		this.configCacheName = properties.getFilterConfigCacheName();
	}

	@Bean
	@ConditionalOnMissingBean(AsyncCacheResolver.class)
	public AsyncCacheResolver hazelcastCacheResolver() {
		return new HazelcastCacheResolver(hazelcastInstance, true);
	}

	@Bean
	@ConditionalOnMissingBean(CacheManager.class)
	@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "filter-config-caching-enabled", havingValue = "true")
	public CacheManager<String, Bucket4JConfiguration> configCacheManager() {
		IMap<String, Bucket4JConfiguration> map = hazelcastInstance.getMap(configCacheName);
		return new HazelcastCacheManager<>(map);
	}

	@Bean
	@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "filter-config-caching-enabled", havingValue = "true")
	public HazelcastCacheListener<String, Bucket4JConfiguration> configCacheListener() {
		IMap<String, Bucket4JConfiguration> map = hazelcastInstance.getMap(configCacheName);
		return new HazelcastCacheListener<>(map);
	}

}
