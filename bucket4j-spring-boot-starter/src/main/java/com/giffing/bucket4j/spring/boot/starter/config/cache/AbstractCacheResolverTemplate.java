package com.giffing.bucket4j.spring.boot.starter.config.cache;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimitResult;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitResultWrapper;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricBucketListener;
import io.github.bucket4j.*;
import io.github.bucket4j.distributed.AsyncBucketProxy;
import io.github.bucket4j.distributed.proxy.AbstractProxyManager;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
public abstract class AbstractCacheResolverTemplate<T> {

    public ProxyManagerWrapper resolve(String cacheName) {
        AbstractProxyManager<T> proxyManager = getProxyManager(cacheName);
        return (key, numTokens, estimate, bucketConfiguration, metricsListener, version, replaceStrategy) -> {

            if(isAsync()) {
                AsyncBucketProxy bucket = getAsyncBucketProxy(key, bucketConfiguration, metricsListener, version, replaceStrategy, proxyManager);
                CompletableFuture<RateLimitResult> result;
                if (estimate) {
                    result = getAsyncEstimatedRateLimit(key, numTokens, estimate, bucket);
                } else {
                    result = getAsyncRateLimit(numTokens, bucket);
                }
                return new RateLimitResultWrapper(result);
            } else {
                Bucket bucket = getSyncBucket(key, bucketConfiguration, metricsListener, version, replaceStrategy, proxyManager);
                log.debug("execute-rate-limit;sync:{};key:{};numTokens:{};estimate:{}", false, key, numTokens, estimate);
                if (estimate) {
                    return getSyncEstimatedRateLimit(numTokens, bucket);
                } else {
                    return getSyncRateLimit(numTokens, bucket);
                }
            }
        };
    }

    public abstract T castStringToCacheKey(String key);

    public abstract boolean isAsync();

    public abstract AbstractProxyManager<T> getProxyManager(String cacheName);

    private RateLimitResultWrapper getSyncRateLimit(Integer numTokens, Bucket bucket) {
        log.debug("consume-token");
        var consumptionProbe = bucket.tryConsumeAndReturnRemaining(numTokens);
        var result = mapToRateLimitResult(consumptionProbe);
        return new RateLimitResultWrapper(result);
    }

    private RateLimitResultWrapper getSyncEstimatedRateLimit(Integer numTokens, Bucket bucket) {
        var estimatedConsumptionProbe = bucket.estimateAbilityToConsume(numTokens);
        if(estimatedConsumptionProbe.canBeConsumed()) {
            log.debug("estimation-can-consume no token taken");
            var result = mapToRateLimitResult(estimatedConsumptionProbe);
            return new RateLimitResultWrapper(result);
        } else {
            log.debug("estimation-cannot-consume take tokens");
            var consumptionProbe = bucket.tryConsumeAndReturnRemaining(numTokens);
            var result = mapToRateLimitResult(consumptionProbe);
            return new RateLimitResultWrapper(result);
        }
    }

    private CompletableFuture<RateLimitResult> getAsyncRateLimit(Integer numTokens, AsyncBucketProxy bucket) {
        CompletableFuture<RateLimitResult> result;
        result = bucket.tryConsumeAndReturnRemaining(numTokens)
                .thenApply(consumptionProbe-> {
                    log.debug("consume-token");
                    return mapToRateLimitResult(consumptionProbe);
                });
        return result;
    }

    private CompletableFuture<RateLimitResult> getAsyncEstimatedRateLimit(String key, Integer numTokens, boolean estimate, AsyncBucketProxy bucket) {
        CompletableFuture<RateLimitResult> result;
        result = bucket.estimateAbilityToConsume(numTokens)
                .thenCompose(ecp -> {
                    log.debug("execute-rate-limit;sync:{};key:{};numTokens:{};estimate:{}", true, key, numTokens, estimate);
                    if (ecp.canBeConsumed()) {
                        log.debug("estimation-can-consume no token taken");
                        return CompletableFuture.completedFuture(mapToRateLimitResult(ecp));
                    } else {
                        log.debug("estimation-cannot-consume take tokens");
                        return bucket.tryConsumeAndReturnRemaining(numTokens)
                                .thenApply(this::mapToRateLimitResult);
                    }
                });
        return result;
    }

    private Bucket getSyncBucket(String key, BucketConfiguration bucketConfiguration, MetricBucketListener metricsListener, long version, TokensInheritanceStrategy replaceStrategy, AbstractProxyManager<T> proxyManager) {
        return proxyManager
                .builder()
                .withImplicitConfigurationReplacement(version, replaceStrategy)
                .build(castStringToCacheKey(key), () -> bucketConfiguration)
                .toListenable(metricsListener);
    }

    private AsyncBucketProxy getAsyncBucketProxy(String key, BucketConfiguration bucketConfiguration, MetricBucketListener metricsListener, long version, TokensInheritanceStrategy replaceStrategy, AbstractProxyManager<T> proxyManager) {
        return proxyManager.asAsync()
                .builder()
                .withImplicitConfigurationReplacement(version, replaceStrategy)
                .build(castStringToCacheKey(key), () -> CompletableFuture.completedFuture(bucketConfiguration))
                .toListenable(metricsListener);
    }

    private RateLimitResult mapToRateLimitResult(EstimationProbe estimatedConsumptionProbe) {
        return RateLimitResult
                .builder()
                .estimation(true)
                .consumed(estimatedConsumptionProbe.canBeConsumed())
                .remainingTokens(estimatedConsumptionProbe.getRemainingTokens())
                .nanosToWaitForReset(0)
                .nanosToWaitForRefill(estimatedConsumptionProbe.getNanosToWaitForRefill())
                .build();
    }

    private RateLimitResult mapToRateLimitResult(ConsumptionProbe cp) {
        return RateLimitResult
                .builder()
                .estimation(false)
                .consumed(cp.isConsumed())
                .remainingTokens(cp.getRemainingTokens())
                .nanosToWaitForReset(cp.getNanosToWaitForReset())
                .nanosToWaitForRefill(cp.getNanosToWaitForRefill())
                .build();
    }


}
