package com.giffing.bucket4j.spring.boot.starter.config.cache.jcache;

import javax.cache.Cache;
import javax.cache.CacheManager;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.exception.JCacheNotFoundException;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.grid.jcache.JCacheProxyManager;

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
	
	public ProxyManager<String> resolve(String cacheName) {
		Cache<String, byte[]> springCache = cacheManager.getCache(cacheName);
		if (springCache == null) {
			throw new JCacheNotFoundException(cacheName);
		}

		return new JCacheProxyManager<>(springCache);
	}
	
}
