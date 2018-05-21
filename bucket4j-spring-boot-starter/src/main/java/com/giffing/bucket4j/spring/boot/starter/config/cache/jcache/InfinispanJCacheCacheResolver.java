package com.giffing.bucket4j.spring.boot.starter.config.cache.jcache;

import org.infinispan.Cache;
import org.infinispan.functional.impl.FunctionalMapImpl;
import org.infinispan.functional.impl.ReadWriteMapImpl;
import org.infinispan.manager.CacheContainer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.exception.JCacheNotFoundException;

import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.grid.ProxyManager;
import io.github.bucket4j.grid.infinispan.Infinispan;


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
		return Bucket4j.extension(Infinispan.class).proxyManagerForMap(ReadWriteMapImpl.create(functionalMap));
	}

}
