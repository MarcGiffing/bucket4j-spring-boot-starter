package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.redisson;

import java.time.Duration;

import org.redisson.command.CommandExecutor;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.ProxyManagerWrapper;
import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.context.ConsumptionProbeHolder;

import io.github.bucket4j.distributed.AsyncBucketProxy;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.redisson.cas.RedissonBasedProxyManager;

/**
 * This class is the Redis implementation of the {@link CacheResolver}.
 *
 */
public class RedissonCacheResolver implements SyncCacheResolver {
	
	private final CommandExecutor commandExecutor;
	
	public RedissonCacheResolver(CommandExecutor commandExecutor) {
		this.commandExecutor = commandExecutor;
	}
	
	public ProxyManagerWrapper resolve(String cacheName) {
		final ProxyManager<String> proxyManager =  RedissonBasedProxyManager.builderFor(commandExecutor)
				.withExpirationStrategy(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofSeconds(10)))
				.build();
		
		return (key, numTokens, bucketConfiguration, metricsListener) -> {
			AsyncBucketProxy bucket = proxyManager.asAsync().builder().build(key, bucketConfiguration).toListenable(metricsListener);
			return new ConsumptionProbeHolder(bucket.tryConsumeAndReturnRemaining(numTokens));
		};
			
	}
}
