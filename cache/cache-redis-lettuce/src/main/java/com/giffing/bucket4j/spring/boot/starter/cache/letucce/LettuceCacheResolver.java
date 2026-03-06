package com.giffing.bucket4j.spring.boot.starter.cache.letucce;

import com.giffing.bucket4j.spring.boot.starter.core.cache.AbstractCacheResolverTemplate;
import com.giffing.bucket4j.spring.boot.starter.core.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.core.cache.CacheResolver;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.AbstractProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.RedisClient;

import java.time.Duration;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This class is the Redis implementation of the {@link CacheResolver}.
 */
public class LettuceCacheResolver extends AbstractCacheResolverTemplate<byte[]> implements AsyncCacheResolver {

    private final RedisClient redisClient;

    public LettuceCacheResolver(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public AbstractProxyManager<byte[]> getProxyManager(String cacheName) {
        return Bucket4jLettuce.casBasedBuilder(redisClient)
                .expirationAfterWrite(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofSeconds(10)))
                .build();
    }

    @Override
    public byte[] castStringToCacheKey(String key) {
        return key.getBytes(UTF_8);
    }
}
