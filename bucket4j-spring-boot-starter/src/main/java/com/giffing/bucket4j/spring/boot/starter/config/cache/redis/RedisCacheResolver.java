package com.giffing.bucket4j.spring.boot.starter.config.cache.redis;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * This class is the Redis implementation of the {@link CacheResolver}.
 *
 */
public class RedisCacheResolver implements SyncCacheResolver {

	private RedisTemplate<String, byte[]> redisTemplate;

	public RedisCacheResolver(RedisTemplate<String, byte[]> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}
	
	public ProxyManager<String> resolve(String cacheName) {
		return new RedisProxyManager(redisTemplate, cacheName);
	}
}
