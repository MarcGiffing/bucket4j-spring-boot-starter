package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.jedis;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.ProxyManagerWrapper;
import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.context.ConsumptionProbeHolder;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.jedis.cas.JedisBasedProxyManager;
import java.time.Duration;
import redis.clients.jedis.JedisPool;

/**
 * This class is the Redis implementation of the {@link CacheResolver}.
 *
 */
public class JedisCacheResolver implements SyncCacheResolver {
	
	private final JedisPool pool;
	
	public JedisCacheResolver(JedisPool pool) {
		this.pool = pool;
	}
	
	@Override 
	public ProxyManagerWrapper resolve(String cacheName) {
		final ProxyManager<byte[]> proxyManager =  JedisBasedProxyManager.builderFor(pool)
				.withExpirationStrategy(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofSeconds(10)))
				.build();
		
		return (key, numTokens, bucketConfiguration, metricsListener) -> {
			Bucket bucket = proxyManager.builder().build(key.getBytes(UTF_8), bucketConfiguration).toListenable(metricsListener);
			return new ConsumptionProbeHolder(bucket.tryConsumeAndReturnRemaining(numTokens));
		};
			
	}

	@Override
	public CacheManager<String, Bucket4JConfiguration> resolveConfigCacheManager(String cacheName) {
		return null;
	}
}
