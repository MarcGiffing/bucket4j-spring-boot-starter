package com.giffing.bucket4j.spring.boot.starter.config.cache.jcache;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import org.infinispan.Cache;
import org.infinispan.functional.impl.FunctionalMapImpl;
import org.infinispan.functional.impl.ReadWriteMapImpl;
import org.infinispan.manager.CacheContainer;

import com.giffing.bucket4j.spring.boot.starter.config.cache.ProxyManagerWrapper;
import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.context.ConsumptionProbeHolder;
import com.giffing.bucket4j.spring.boot.starter.exception.JCacheNotFoundException;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.grid.infinispan.InfinispanProxyManager;

/**
 * To use Infinispan you need a special bucket4j-infinispan dependency.
 *  
 * https://github.com/vladimir-bukhtoyarov/bucket4j/blob/master/doc-pages/infinispan.md
 * 
 * Question: Bucket4j already supports JCache since version 1.2. Why it was needed to introduce direct support for Infinispan?
 * 
 * Answer: When you want to use Bucket4j together with Infinispan, you must always use bucket4j-infinispan module instead of bucket4j-jcache,
 * because Infinispan does not provide mutual exclusion for entry-processors. Any attempt to use Infinispan via bucket4j-jcache will be 
 * failed with UnsupportedOperationException exception at bucket construction time.
 *
 */
public class InfinispanJCacheCacheResolver implements SyncCacheResolver {

	private final CacheContainer cacheContainer;

	public InfinispanJCacheCacheResolver(CacheContainer cacheContainer) {
		this.cacheContainer = cacheContainer;
	}

	public ProxyManagerWrapper resolve(String cacheName) {
		Cache<String, byte[]> cache = cacheContainer.getCache(cacheName);
		if (cache == null) {
			throw new JCacheNotFoundException(cacheName);
		}

		FunctionalMapImpl<String, byte[]> functionalMap = FunctionalMapImpl.create(cache.getAdvancedCache());
		InfinispanProxyManager<String> infinispanProxyManager = new InfinispanProxyManager<>(ReadWriteMapImpl.create(functionalMap));
		return (key, numTokens, bucketConfiguration, metricsListener, version, replaceStrategy) -> {
			Bucket bucket = infinispanProxyManager.builder()
					.withImplicitConfigurationReplacement(version, replaceStrategy)
					.build(key, bucketConfiguration).toListenable(metricsListener);
			return new ConsumptionProbeHolder(bucket.tryConsumeAndReturnRemaining(numTokens));
		};
	}

	@Override
	public CacheManager<String, Bucket4JConfiguration> resolveConfigCacheManager(String cacheName) {
		return null;
	}

}
