package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.lettuce;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.time.Duration;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.ProxyManagerWrapper;
import com.giffing.bucket4j.spring.boot.starter.context.ConsumptionProbeHolder;

import io.github.bucket4j.distributed.AsyncBucketProxy;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;

/**
 * This class is the Redis implementation of the {@link CacheResolver}.
 */
public class LettuceCacheResolver implements AsyncCacheResolver {

	private final RedisClient redisClient;

	public LettuceCacheResolver(RedisClient redisClient) {
		this.redisClient = redisClient;
	}

	@Override
	public ProxyManagerWrapper resolve(String cacheName) {
		final ProxyManager<byte[]> proxyManager = LettuceBasedProxyManager.builderFor(redisClient)
				.withExpirationStrategy(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofSeconds(10)))
				.build();

		return (key, numTokens, bucketConfiguration, metricsListener, version, replaceStrategy) -> {
			AsyncBucketProxy bucket = proxyManager.asAsync().builder()
					.withImplicitConfigurationReplacement(version, replaceStrategy)
					.build(key.getBytes(UTF_8), bucketConfiguration).toListenable(metricsListener);
			return new ConsumptionProbeHolder(bucket.tryConsumeAndReturnRemaining(numTokens));
		};
	}
}
