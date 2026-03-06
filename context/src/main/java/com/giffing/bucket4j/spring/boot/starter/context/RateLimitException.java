package com.giffing.bucket4j.spring.boot.starter.context;

import lombok.Getter;

import java.util.concurrent.TimeUnit;

/**
 * This exception is thrown when the rate limit is reached in the context of a method level when using the
 * {@link RateLimiting} annotation.
 */
@Getter
public class RateLimitException extends RuntimeException {
    private final long retryAfterNanoSeconds;
    private final long remainingTokens;
    private final String configurationName;

    public RateLimitException(Long retryAfterNanoSeconds, Long remainingTokens, String configurationName, String cacheKey) {
        // the slower variant
        super("Rate limit exceeded for configuration %s and cache-key %s : . Retry after: %sms . Remaining tokens: %s".formatted(
                configurationName,
                cacheKey,
                toMilliseconds(retryAfterNanoSeconds),
                toMilliseconds(remainingTokens))
        );
        this.retryAfterNanoSeconds = retryAfterNanoSeconds != null ? retryAfterNanoSeconds : 0;
        this.remainingTokens = remainingTokens != null ? remainingTokens : 0;
        this.configurationName = configurationName;
    }

    private static long toMilliseconds(Long retryAfterNanoSeconds) {
        return retryAfterNanoSeconds != null ? TimeUnit.NANOSECONDS.toMillis(retryAfterNanoSeconds) : 0;
    }

}
