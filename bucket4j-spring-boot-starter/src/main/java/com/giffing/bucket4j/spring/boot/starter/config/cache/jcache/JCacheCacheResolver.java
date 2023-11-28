package com.giffing.bucket4j.spring.boot.starter.config.cache.jcache;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.ProxyManagerWrapper;
import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.context.ConsumptionProbeHolder;
import com.giffing.bucket4j.spring.boot.starter.exception.JCacheNotFoundException;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.grid.jcache.JCacheProxyManager;

import javax.cache.Cache;
import javax.cache.CacheManager;

/**
 * This class is the JCache (JSR-107) implementation of the {@link CacheResolver}.
 * It uses Bucket4Js {@link JCacheProxyManager} to implement the {@link ProxyManager}.
 *
 */
public class JCacheCacheResolver implements SyncCacheResolver {
	
	private CacheManager cacheManager;

	public JCacheCacheResolver(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
	
	public ProxyManagerWrapper resolve(String cacheName) {
		Cache<String, byte[]> springCache = cacheManager.getCache(cacheName);
		if (springCache == null) {
			throw new JCacheNotFoundException(cacheName);
		}

		JCacheProxyManager<String> jCacheProxyManager = new JCacheProxyManager<>(springCache);
		return (key, numTokens, bucketConfiguration, metricsListener, version, replaceStrategy) -> {
			Bucket bucket = jCacheProxyManager.builder()
					.withImplicitConfigurationReplacement(version, replaceStrategy)
					.build(key, bucketConfiguration).toListenable(metricsListener);
			return new ConsumptionProbeHolder(bucket.tryConsumeAndReturnRemaining(numTokens));
		};
	}
}
