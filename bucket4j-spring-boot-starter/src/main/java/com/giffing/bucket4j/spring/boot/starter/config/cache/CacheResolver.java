package com.giffing.bucket4j.spring.boot.starter.config.cache;

import io.github.bucket4j.distributed.proxy.ProxyManager;

/**
 * The CacheResolver is used to resolve Bucket4js {@link ProxyManager} by
 * a given cache name. Each cache implementation should implement this interface.
 * <p>
 * But the interface shouldn't be implemented directly. The CacheResolver is divided
 * to the blocking {@link SyncCacheResolver} and the asynchronous {@link AsyncCacheResolver}.
 *
 */
public interface CacheResolver {

	ProxyManagerWrapper resolve(String cacheName);
}
