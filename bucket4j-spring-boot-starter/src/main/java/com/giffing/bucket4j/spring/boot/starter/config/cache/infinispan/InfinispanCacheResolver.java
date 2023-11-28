package com.giffing.bucket4j.spring.boot.starter.config.cache.infinispan;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.ProxyManagerWrapper;
import com.giffing.bucket4j.spring.boot.starter.context.ConsumptionProbeHolder;
import io.github.bucket4j.distributed.AsyncBucketProxy;
import io.github.bucket4j.grid.infinispan.InfinispanProxyManager;
import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.functional.FunctionalMap;
import org.infinispan.functional.impl.FunctionalMapImpl;
import org.infinispan.functional.impl.ReadWriteMapImpl;
import org.infinispan.manager.CacheContainer;

public class InfinispanCacheResolver implements AsyncCacheResolver {

	private CacheContainer cacheContainer;
	
	public InfinispanCacheResolver(CacheContainer cacheContainer) {
		this.cacheContainer = cacheContainer;
	}

	@Override
	public ProxyManagerWrapper resolve(String cacheName) {
		Cache<String, byte[]> cache = cacheContainer.getCache(cacheName);
		InfinispanProxyManager<String> infinispanProxyManager = new InfinispanProxyManager<>(toMap(cache));
		return (key, numTokens, bucketConfiguration, metricsListener, version, replaceStrategy) -> {
			AsyncBucketProxy bucket = infinispanProxyManager.asAsync().builder()
					.withImplicitConfigurationReplacement(version, replaceStrategy)
					.build(key, bucketConfiguration).toListenable(metricsListener);
			return new ConsumptionProbeHolder(bucket.tryConsumeAndReturnRemaining(numTokens));
		};
	}
	private static FunctionalMap.ReadWriteMap<String, byte[]> toMap(Cache<String, byte[]> cache) {
		AdvancedCache<String, byte[]> advancedCache = cache.getAdvancedCache();
		FunctionalMapImpl<String, byte[]> functionalMap = FunctionalMapImpl.create(advancedCache);
		return ReadWriteMapImpl.create(functionalMap);
	}
}
