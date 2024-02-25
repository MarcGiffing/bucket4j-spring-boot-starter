package com.giffing.bucket4j.spring.boot.starter.config.cache;

import com.giffing.bucket4j.spring.boot.starter.context.ConsumptionProbeHolder;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.distributed.AsyncBucketProxy;
import io.github.bucket4j.distributed.proxy.AbstractProxyManager;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractCacheResolverTemplate<T> {

    public ProxyManagerWrapper resolve(String cacheName) {
        AbstractProxyManager<T> proxyManager = getProxyManager(cacheName);
        return ((key, numTokens, bucketConfiguration, metricsListener, version, replaceStrategy) -> {
            if(isAsync()) {
                AsyncBucketProxy bucket = proxyManager.asAsync()
                        .builder()
                        .withImplicitConfigurationReplacement(version, replaceStrategy)
                        .build(castStringToCacheKey(key), () -> CompletableFuture.completedFuture(bucketConfiguration))
                        .toListenable(metricsListener);
                return new ConsumptionProbeHolder(bucket.tryConsumeAndReturnRemaining(numTokens));
            } else {
                Bucket bucket = proxyManager
                        .builder()
                        .withImplicitConfigurationReplacement(version, replaceStrategy)
                        .build(castStringToCacheKey(key), () -> bucketConfiguration)
                        .toListenable(metricsListener);
                return new ConsumptionProbeHolder(bucket.tryConsumeAndReturnRemaining(numTokens));
            }
        });
    }

    public abstract T castStringToCacheKey(String key);

    public abstract boolean isAsync();

    public abstract AbstractProxyManager<T> getProxyManager(String cacheName);



}
