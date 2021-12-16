package com.giffing.bucket4j.spring.boot.starter.config.cache.infinispan;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.grid.infinispan.InfinispanProxyManager;
import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.functional.FunctionalMap;
import org.infinispan.functional.impl.FunctionalMapImpl;
import org.infinispan.functional.impl.ReadWriteMapImpl;
import org.infinispan.manager.CacheContainer;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;

import io.github.bucket4j.Bucket4j;

public class InfinispanCacheResolver implements AsyncCacheResolver {

	private CacheContainer cacheContainer;
	
	public InfinispanCacheResolver(CacheContainer cacheContainer) {
		this.cacheContainer = cacheContainer;
	}
	
	@Override
	public ProxyManager<String> resolve(String cacheName) {
		Cache<String, byte[]> cache = cacheContainer.getCache(cacheName);


		// TODO how to create an instance of ReadWriteMap
		return new InfinispanProxyManager<>(toMap(cache));
	}

	private static FunctionalMap.ReadWriteMap<String, byte[]> toMap(Cache<String, byte[]> cache) {
		AdvancedCache<String, byte[]> advancedCache = cache.getAdvancedCache();
		FunctionalMapImpl<String, byte[]> functionalMap = FunctionalMapImpl.create(advancedCache);
		return ReadWriteMapImpl.create(functionalMap);
	}

}
