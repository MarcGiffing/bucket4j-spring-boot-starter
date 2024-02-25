package com.giffing.bucket4j.spring.boot.starter.config.cache.ignite;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AbstractCacheResolverTemplate;
import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import io.github.bucket4j.distributed.proxy.AbstractProxyManager;
import io.github.bucket4j.grid.ignite.thick.IgniteProxyManager;
import org.apache.ignite.Ignite;

public class IgniteCacheResolver extends AbstractCacheResolverTemplate<String> implements AsyncCacheResolver {

	private final Ignite ignite;
	
	public IgniteCacheResolver(Ignite ignite) {
		this.ignite = ignite;
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
		org.apache.ignite.IgniteCache<String, byte[]> cache = ignite.cache(cacheName);
		return new IgniteProxyManager<>(cache);
	}
}
