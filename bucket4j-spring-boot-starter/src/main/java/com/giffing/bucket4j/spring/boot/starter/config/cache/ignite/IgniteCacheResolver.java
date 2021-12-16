package com.giffing.bucket4j.spring.boot.starter.config.cache.ignite;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.grid.ignite.thick.IgniteProxyManager;
import org.apache.ignite.Ignite;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;

public class IgniteCacheResolver implements AsyncCacheResolver {

	private Ignite ignite;
	
	public IgniteCacheResolver(Ignite ignite) {
		this.ignite = ignite;
	}
	
	@Override
	public ProxyManager<String> resolve(String cacheName) {
		org.apache.ignite.IgniteCache<String, byte[]> cache = ignite.cache(cacheName);
		return new IgniteProxyManager(cache);
	}

}
