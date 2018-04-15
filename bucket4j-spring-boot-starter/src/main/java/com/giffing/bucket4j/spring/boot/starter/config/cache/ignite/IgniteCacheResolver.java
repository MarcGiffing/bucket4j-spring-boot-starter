package com.giffing.bucket4j.spring.boot.starter.config.cache.ignite;

import org.apache.ignite.Ignite;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;

import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.grid.GridBucketState;
import io.github.bucket4j.grid.ProxyManager;

public class IgniteCacheResolver implements AsyncCacheResolver {

	private Ignite ignite;
	
	public IgniteCacheResolver(Ignite ignite) {
		this.ignite = ignite;
	}
	
	@Override
	public ProxyManager<String> resolve(String cacheName) {
		org.apache.ignite.IgniteCache<String, GridBucketState> cache = ignite.cache(cacheName);
		return Bucket4j.extension(io.github.bucket4j.grid.ignite.Ignite.class).proxyManagerForCache(cache);
	}

}
