package com.giffing.bucket4j.spring.boot.starter.config.cache.ignite;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.config.cache.ProxyManagerWrapper;
import com.giffing.bucket4j.spring.boot.starter.context.ConsumptionProbeHolder;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;

import io.github.bucket4j.distributed.AsyncBucketProxy;
import io.github.bucket4j.grid.ignite.thick.IgniteProxyManager;
import org.apache.ignite.Ignite;

public class IgniteCacheResolver implements AsyncCacheResolver {

	private final Ignite ignite;
	
	public IgniteCacheResolver(Ignite ignite) {
		this.ignite = ignite;
	}
	
	@Override
	public ProxyManagerWrapper resolve(String cacheName) {
		org.apache.ignite.IgniteCache<String, byte[]> cache = ignite.cache(cacheName);
		IgniteProxyManager<String> igniteProxyManager = new IgniteProxyManager<>(cache);
		return (key, numTokens, bucketConfiguration, metricsListener, version, replaceStrategy) -> {
			AsyncBucketProxy bucket = igniteProxyManager.asAsync().builder()
					.withImplicitConfigurationReplacement(version, replaceStrategy)
					.build(key, bucketConfiguration).toListenable(metricsListener);
			return new ConsumptionProbeHolder(bucket.tryConsumeAndReturnRemaining(numTokens));
		};
	}

	@Override
	public CacheManager<String, Bucket4JConfiguration> resolveConfigCacheManager(String cacheName) {
		return new IgniteCacheManager<>(ignite.cache(cacheName));
	}

}
