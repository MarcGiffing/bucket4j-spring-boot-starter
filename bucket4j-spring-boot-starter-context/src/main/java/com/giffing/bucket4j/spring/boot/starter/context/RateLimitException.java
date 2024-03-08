package com.giffing.bucket4j.spring.boot.starter.context;

/**
 * This exception is thrown when the rate limit is reached in the context of a method level when using the
 * {@link RateLimiting} annotation.
 */
public class RateLimitException extends RuntimeException {
}
