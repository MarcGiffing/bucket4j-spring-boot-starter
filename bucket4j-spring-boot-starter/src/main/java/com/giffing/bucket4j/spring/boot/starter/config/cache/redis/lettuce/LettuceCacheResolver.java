package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.lettuce;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.ProxyManagerWrapper;
import com.giffing.bucket4j.spring.boot.starter.context.ConsumptionProbeHolder;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import io.github.bucket4j.distributed.AsyncBucketProxy;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import java.time.Duration;

/**
 * This class is the Redis implementation of the {@link CacheResolver}.
 *
 */
public class LettuceCacheResolver implements AsyncCacheResolver {
	
	private final RedisClient redisClient;
	
	public LettuceCacheResolver(RedisClient redisClient) {
		this.redisClient = redisClient;
	}
	
	@Override 
	public ProxyManagerWrapper resolve(String cacheName) {
		final ProxyManager<byte[]> proxyManager =  LettuceBasedProxyManager.builderFor(redisClient)
				.withExpirationStrategy(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofSeconds(10)))
				.build();
		
		return (key, numTokens, bucketConfiguration, metricsListener, version, replaceStrategy) -> {
			AsyncBucketProxy bucket = proxyManager.asAsync().builder()
					.withImplicitConfigurationReplacement(version, replaceStrategy)
					.build(key.getBytes(UTF_8), bucketConfiguration).toListenable(metricsListener);
			return new ConsumptionProbeHolder(bucket.tryConsumeAndReturnRemaining(numTokens));
		};
			
	}

	@Override
	public CacheManager<String, Bucket4JConfiguration> resolveConfigCacheManager(String cacheName) {
		return new LettuceCacheManager<>(redisClient, cacheName, String.class, Bucket4JConfiguration.class);
	}
}
