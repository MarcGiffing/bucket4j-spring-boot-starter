package com.giffing.bucket4j.spring.boot.starter.config.cache.infinispan;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AbstractCacheResolverTemplate;
import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import io.github.bucket4j.distributed.proxy.AbstractProxyManager;
import io.github.bucket4j.grid.infinispan.InfinispanProxyManager;
import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.functional.FunctionalMap;
import org.infinispan.functional.impl.FunctionalMapImpl;
import org.infinispan.functional.impl.ReadWriteMapImpl;
import org.infinispan.manager.CacheContainer;

public class InfinispanCacheResolver extends AbstractCacheResolverTemplate<String> implements AsyncCacheResolver {

	private final CacheContainer cacheContainer;
	
	public InfinispanCacheResolver(CacheContainer cacheContainer) {
		this.cacheContainer = cacheContainer;
	}

	@Override
	public String castStringToCacheKey(String key) {
		return key;
	}

	@Override
	public boolean isAsync() {
		return true;
	}

	@Override
	public AbstractProxyManager<String> getProxyManager(String cacheName) {
		Cache<String, byte[]> cache = cacheContainer.getCache(cacheName);
		return new InfinispanProxyManager<>(toMap(cache));
	}

	private static FunctionalMap.ReadWriteMap<String, byte[]> toMap(Cache<String, byte[]> cache) {
		AdvancedCache<String, byte[]> advancedCache = cache.getAdvancedCache();
		FunctionalMapImpl<String, byte[]> functionalMap = FunctionalMapImpl.create(advancedCache);
		return ReadWriteMapImpl.create(functionalMap);
	}
}
