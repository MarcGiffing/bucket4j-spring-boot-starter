package com.giffing.bucket4j.spring.boot.starter.context;

/**
 * This exception is thrown when the rate limit is reached in the context of a method level when using the
 * {@link RateLimiting} annotation.
 */
public class RateLimitException extends RuntimeException {
    private final long retryAfterNanoSeconds;
    private final long remainingTokens;
    private final String configurationName;

    public RateLimitException(Long retryAfterNanoSeconds, Long remainingTokens, String configurationName) {
        super("Rate limit exceeded for configuration: " + configurationName + ". Retry after: " + (retryAfterNanoSeconds != null ? retryAfterNanoSeconds : 0) + "ns. Remaining tokens: " + (remainingTokens != null ? remainingTokens : 0));
        this.retryAfterNanoSeconds = retryAfterNanoSeconds != null ? retryAfterNanoSeconds : 0;
        this.remainingTokens = remainingTokens != null ? remainingTokens : 0;
        this.configurationName = configurationName;
    }

    public long getRetryAfterNanoSeconds() {
        return retryAfterNanoSeconds;
    }

    public long getRemainingTokens() {
        return remainingTokens;
    }

    public String getConfigurationName() {
        return configurationName;
    }
}
