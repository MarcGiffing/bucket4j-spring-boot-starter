package com.giffing.bucket4j.spring.boot.starter.cache.infinispan;

import com.giffing.bucket4j.spring.boot.starter.autoconfigure.exception.JCacheNotFoundException;
import com.giffing.bucket4j.spring.boot.starter.core.cache.AbstractCacheResolverTemplate;
import com.giffing.bucket4j.spring.boot.starter.core.cache.SyncCacheResolver;
import io.github.bucket4j.distributed.proxy.AbstractProxyManager;
import io.github.bucket4j.grid.infinispan.Bucket4jInfinispan;
import org.infinispan.Cache;
import org.infinispan.functional.impl.FunctionalMapImpl;
import org.infinispan.functional.impl.ReadWriteMapImpl;
import org.infinispan.manager.CacheContainer;

/**
 * To use Infinispan you need a special bucket4j-infinispan dependency.
 * <p>
 * Question: Bucket4j already supports JCache since version 1.2. Why it was needed to introduce direct support for Infinispan?
 * <p>
 * Answer: When you want to use Bucket4j together with Infinispan, you must always use bucket4j-infinispan module instead of bucket4j-jcache,
 * because Infinispan does not provide mutual exclusion for entry-processors. Any attempt to use Infinispan via bucket4j-jcache will be
 * failed with UnsupportedOperationException exception at bucket construction time.
 */
public class InfinispanJCacheCacheResolver extends AbstractCacheResolverTemplate<String> implements SyncCacheResolver {

    private final CacheContainer cacheContainer;

    public InfinispanJCacheCacheResolver(CacheContainer cacheContainer) {
        this.cacheContainer = cacheContainer;
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
        Cache<String, byte[]> cache = cacheContainer.getCache(cacheName);
        if (cache == null) {
            throw new JCacheNotFoundException(cacheName);
        }
        FunctionalMapImpl<String, byte[]> functionalMap = FunctionalMapImpl.create(cache.getAdvancedCache());
        return Bucket4jInfinispan.entryProcessorBasedBuilder(ReadWriteMapImpl.create(functionalMap)).build();
    }
}
