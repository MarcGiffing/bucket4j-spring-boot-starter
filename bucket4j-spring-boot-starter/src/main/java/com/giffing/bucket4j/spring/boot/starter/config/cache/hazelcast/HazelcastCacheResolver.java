
package com.giffing.bucket4j.spring.boot.starter.config.cache.hazelcast;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.config.cache.ProxyManagerWrapper;
import com.giffing.bucket4j.spring.boot.starter.context.ConsumptionProbeHolder;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.distributed.AsyncBucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.grid.hazelcast.HazelcastProxyManager;

/**
 * Creates the {@link ProxyManager} with Bucket4js {@link HazelcastProxyManager} class.
 * It uses the {@link HazelcastInstance} to retrieve the needed cache. 
 *
 */
public class HazelcastCacheResolver implements AsyncCacheResolver {

	private final HazelcastInstance hazelcastInstance;
	
	private final boolean async;

	public HazelcastCacheResolver(HazelcastInstance hazelcastInstance, boolean async) {
		this.hazelcastInstance = hazelcastInstance;
		this.async = async;
	}
	
	@Override
	public ProxyManagerWrapper resolve(String cacheName) {
		IMap<String, byte[]> map = hazelcastInstance.getMap(cacheName);
		HazelcastProxyManager<String> hazelcastProxyManager = new HazelcastProxyManager<>(map);
		return (key, numTokens, bucketConfiguration, metricsListener) -> {
			if(async) {
				AsyncBucketProxy bucket = hazelcastProxyManager.asAsync().builder().build(key, bucketConfiguration).toListenable(metricsListener);
				return new ConsumptionProbeHolder(bucket.tryConsumeAndReturnRemaining(numTokens));	
			} else {
				Bucket bucket = hazelcastProxyManager.builder().build(key, bucketConfiguration).toListenable(metricsListener);
				return new ConsumptionProbeHolder(bucket.tryConsumeAndReturnRemaining(numTokens));
			}
			
		};
	}
	@Override
	public CacheManager<String, Bucket4JConfiguration> resolveConfigCacheManager(String cacheName){
		IMap<String, Bucket4JConfiguration> map = hazelcastInstance.getMap(cacheName);
		return new HazelcastCacheManager<>(map);
	}
}
