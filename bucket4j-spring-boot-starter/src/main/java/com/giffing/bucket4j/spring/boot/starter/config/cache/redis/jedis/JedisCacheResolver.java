package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.jedis;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AbstractCacheResolverTemplate;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.AbstractProxyManager;
import io.github.bucket4j.redis.jedis.cas.JedisBasedProxyManager;
import redis.clients.jedis.JedisPool;

import java.time.Duration;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This class is the Redis implementation of the {@link CacheResolver}.
 *
 */
public class JedisCacheResolver extends AbstractCacheResolverTemplate<byte[]> implements SyncCacheResolver {
	
	private final JedisPool pool;
	
	public JedisCacheResolver(JedisPool pool) {
		this.pool = pool;
	}
	
	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	public byte[] castStringToCacheKey(String key) {
		return key.getBytes(UTF_8);
	}

	@Override
	public AbstractProxyManager<byte[]> getProxyManager(String cacheName) {
		return JedisBasedProxyManager.builderFor(pool)
				.withExpirationStrategy(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofSeconds(10)))
				.build();
	}
}
