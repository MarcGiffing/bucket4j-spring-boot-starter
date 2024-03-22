package com.giffing.bucket4j.spring.boot.starter.context;

import lombok.Data;

import java.util.concurrent.CompletableFuture;

/**
 * A wrapper class to distinguish the {@link RateLimiting} result between a synchronous  and asynchronous call.
 * <p>
 * If possible we should get rid of it...
 */
@Data
public class RateLimitResultWrapper {

    private RateLimitResult rateLimitResult;

    private CompletableFuture<RateLimitResult> rateLimitResultCompletableFuture;

    public RateLimitResultWrapper(RateLimitResult rateLimitResult) {
        this.rateLimitResult = rateLimitResult;
    }

    public RateLimitResultWrapper(CompletableFuture<RateLimitResult> rateLimitResultCompletableFuture) {
        this.rateLimitResultCompletableFuture = rateLimitResultCompletableFuture;
    }

}
