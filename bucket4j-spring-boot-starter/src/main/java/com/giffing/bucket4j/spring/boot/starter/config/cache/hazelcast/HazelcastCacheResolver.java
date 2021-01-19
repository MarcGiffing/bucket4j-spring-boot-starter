
package com.giffing.bucket4j.spring.boot.starter.config.cache.hazelcast;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.grid.GridBucketState;
import io.github.bucket4j.grid.ProxyManager;
import io.github.bucket4j.grid.hazelcast.Hazelcast;

/**
 * Creates the {@link ProxyManager} with Bucket4js {@link Hazelcast} class.
 * It uses the {@link HazelcastInstance} to retrieve the needed cache. 
 *
 */
public class HazelcastCacheResolver implements AsyncCacheResolver {

	private HazelcastInstance hazelcastInstance;

	public HazelcastCacheResolver(HazelcastInstance hazelcastInstance) {
		this.hazelcastInstance = hazelcastInstance;
	}
	
	@Override
	public ProxyManager<String> resolve(String cacheName) {
		IMap<String, GridBucketState> map = hazelcastInstance.getMap(cacheName);
		return Bucket4j.extension(Hazelcast.class).proxyManagerForMap(map);
	}

}
