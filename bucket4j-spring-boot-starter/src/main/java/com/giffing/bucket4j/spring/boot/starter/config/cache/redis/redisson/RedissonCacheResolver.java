package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.redisson;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AbstractCacheResolverTemplate;
import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheResolver;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.AbstractProxyManager;
import io.github.bucket4j.redis.redisson.cas.RedissonBasedProxyManager;
import org.redisson.command.CommandAsyncExecutor;

import java.time.Duration;

/**
 * This class is the Redis implementation of the {@link CacheResolver}.
 */
public class RedissonCacheResolver extends AbstractCacheResolverTemplate<String> implements AsyncCacheResolver {

	private final CommandAsyncExecutor commandExecutor;

	public RedissonCacheResolver(CommandAsyncExecutor commandExecutor) {
		this.commandExecutor = commandExecutor;
	}

	@Override
	public String castStringToCacheKey(String key) {
		return key;
	}

	@Override
	public boolean isAsync() {
		return true;
	}

	@Override
	public AbstractProxyManager<String> getProxyManager(String cacheName) {
		return RedissonBasedProxyManager.builderFor(commandExecutor)
				.withExpirationStrategy(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofSeconds(10)))
				.build();
	}
}
