package com.giffing.bucket4j.spring.boot.starter.context;

/**
 * Bad name :-)
 * <p>
 * If multiple rate limits configured this strategy decides when to stop the evaluation.
 */
public enum RateLimitConditionMatchingStrategy {

    /**
     * All rate limits should be evaluated
     */
    ALL,
    /**
     * Only the first matching rate limit will be evaluated
     */
    FIRST,

}
