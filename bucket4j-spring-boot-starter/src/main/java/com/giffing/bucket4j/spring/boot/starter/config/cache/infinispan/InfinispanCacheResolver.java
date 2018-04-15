package com.giffing.bucket4j.spring.boot.starter.config.cache.infinispan;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;

import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.grid.ProxyManager;

public class InfinispanCacheResolver implements AsyncCacheResolver {

	private CacheContainer cacheContainer;
	
	public InfinispanCacheResolver(CacheContainer cacheContainer) {
		this.cacheContainer = cacheContainer;
	}
	
	@Override
	public ProxyManager<String> resolve(String cacheName) {
		Cache<Object, Object> cache = cacheContainer.getCache(cacheName);
		// TODO how to create an instance of ReadWriteMap
		return Bucket4j.extension(io.github.bucket4j.grid.infinispan.Infinispan.class).proxyManagerForMap(null);
	}

}
