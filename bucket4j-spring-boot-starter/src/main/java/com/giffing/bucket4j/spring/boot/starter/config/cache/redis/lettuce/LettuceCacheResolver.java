package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.lettuce;

import java.time.Duration;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.ProxyManagerWrapper;
import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.context.ConsumptionProbeHolder;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.distributed.AsyncBucketProxy;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.jedis.cas.JedisBasedProxyManager;
import io.github.bucket4j.redis.jedis.cas.JedisBasedProxyManager.JedisBasedProxyManagerBuilder;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import redis.clients.jedis.JedisPool;

/**
 * This class is the Redis implementation of the {@link CacheResolver}.
 *
 */
public class LettuceCacheResolver implements SyncCacheResolver {
	
	private final RedisClient redisClient;
	
	public LettuceCacheResolver(RedisClient redisClient) {
		this.redisClient = redisClient;
	}
	
	public ProxyManagerWrapper resolve(String cacheName) {
		final ProxyManager<byte[]> proxyManager =  LettuceBasedProxyManager.builderFor(redisClient)
				.withExpirationStrategy(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofSeconds(10)))
				.build();
		
		return (key, numTokens, bucketConfiguration, metricsListener) -> {
			AsyncBucketProxy bucket = proxyManager.asAsync().builder().build(key.getBytes(), bucketConfiguration).toListenable(metricsListener);
			return new ConsumptionProbeHolder(bucket.tryConsumeAndReturnRemaining(numTokens));
		};
			
	}
}
