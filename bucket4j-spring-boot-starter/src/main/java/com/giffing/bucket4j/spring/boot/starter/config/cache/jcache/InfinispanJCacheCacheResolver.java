package com.giffing.bucket4j.spring.boot.starter.config.cache.jcache;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.grid.infinispan.InfinispanProxyManager;
import org.infinispan.Cache;
import org.infinispan.functional.impl.FunctionalMapImpl;
import org.infinispan.functional.impl.ReadWriteMapImpl;
import org.infinispan.manager.CacheContainer;

import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.exception.JCacheNotFoundException;

import io.github.bucket4j.Bucket4j;

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

	private CacheContainer cacheContainer;

	public InfinispanJCacheCacheResolver(CacheContainer cacheContainer) {
		this.cacheContainer = cacheContainer;
	}

	public ProxyManager<String> resolve(String cacheName) {
		Cache<Object, Object> cache = cacheContainer.getCache(cacheName);
		if (cache == null) {
			throw new JCacheNotFoundException(cacheName);
		}

		FunctionalMapImpl functionalMap = FunctionalMapImpl.create(cache.getAdvancedCache());
		return new InfinispanProxyManager<>(ReadWriteMapImpl.create(functionalMap));
	}

}
