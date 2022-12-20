
package com.giffing.bucket4j.spring.boot.starter.config.cache.hazelcast;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.ProxyManagerWrapper;
import com.giffing.bucket4j.spring.boot.starter.context.ConsumptionProbeHolder;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.spring.cache.HazelcastCacheManager;

import io.github.bucket4j.distributed.AsyncBucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.grid.hazelcast.HazelcastProxyManager;

/**
 * Creates the {@link ProxyManager} with Bucket4js {@link HazelcastProxyManager} class.
 * It uses the {@link HazelcastInstance} to retrieve the needed cache. 
 *
 */
public class HazelcastCacheResolver implements AsyncCacheResolver {

	private HazelcastCacheManager hazelcastCacheManager;

	public HazelcastCacheResolver(HazelcastCacheManager hazelcastCacheManager) {
		this.hazelcastCacheManager = hazelcastCacheManager;
	}
	
	@Override
	public ProxyManagerWrapper resolve(String cacheName) {
		IMap<String, byte[]> map = hazelcastCacheManager.getHazelcastInstance().getMap(cacheName);
		HazelcastProxyManager<String> hazelcastProxyManager = new HazelcastProxyManager<>(map);
		return (key, numTokens, bucketConfiguration, metricsListener) -> {
			AsyncBucketProxy bucket = hazelcastProxyManager.asAsync().builder().build(key, bucketConfiguration).toListenable(metricsListener);
			return new ConsumptionProbeHolder(bucket.tryConsumeAndReturnRemaining(numTokens));
		};

	}
}
