package com.giffing.bucket4j.spring.boot.starter.cache.jcache;

import com.giffing.bucket4j.spring.boot.starter.autoconfigure.exception.JCacheNotFoundException;
import com.giffing.bucket4j.spring.boot.starter.core.cache.AbstractCacheResolverTemplate;
import com.giffing.bucket4j.spring.boot.starter.core.cache.CacheResolver;
import com.giffing.bucket4j.spring.boot.starter.core.cache.SyncCacheResolver;
import io.github.bucket4j.distributed.proxy.AbstractProxyManager;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.grid.jcache.JCacheProxyManager;

import javax.cache.Cache;
import javax.cache.CacheManager;

/**
 * This class is the JCache (JSR-107) implementation of the {@link CacheResolver}.
 * It uses Bucket4Js {@link JCacheProxyManager} to implement the {@link ProxyManager}.
 *
 */
public class JCacheCacheResolver extends AbstractCacheResolverTemplate<String> implements SyncCacheResolver {

    private final CacheManager cacheManager;

    public JCacheCacheResolver(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public String castStringToCacheKey(String key) {
        return key;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public AbstractProxyManager<String> getProxyManager(String cacheName) {
        Cache<String, byte[]> springCache = cacheManager.getCache(cacheName);
        if (springCache == null) {
            throw new JCacheNotFoundException(cacheName);
        }
        return new JCacheProxyManager<>(springCache);
    }
}
