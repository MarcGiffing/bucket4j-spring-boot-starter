
package com.giffing.bucket4j.spring.boot.starter.config.cache.hazelcast;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AbstractCacheResolverTemplate;
import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import io.github.bucket4j.distributed.proxy.AbstractProxyManager;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.grid.hazelcast.HazelcastProxyManager;

/**
 * Creates the {@link ProxyManager} with Bucket4js {@link HazelcastProxyManager} class.
 * It uses the {@link HazelcastInstance} to retrieve the needed cache. 
 *
 */
public class HazelcastCacheResolver extends AbstractCacheResolverTemplate<String> implements AsyncCacheResolver {

	private final HazelcastInstance hazelcastInstance;
	
	private final boolean async;

	public HazelcastCacheResolver(HazelcastInstance hazelcastInstance, boolean async) {
		this.hazelcastInstance = hazelcastInstance;
		this.async = async;
	}

	@Override
	public String castStringToCacheKey(String key) {
		return key;
	}

	@Override
	public boolean isAsync() {
		return async;
	}

	@Override
	public AbstractProxyManager<String> getProxyManager(String cacheName) {
		IMap<String, byte[]> map = hazelcastInstance.getMap(cacheName);
		return new HazelcastProxyManager<>(map);
	}
}
