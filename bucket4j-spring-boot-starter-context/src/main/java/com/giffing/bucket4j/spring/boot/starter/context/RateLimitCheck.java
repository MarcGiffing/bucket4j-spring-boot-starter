package com.giffing.bucket4j.spring.boot.starter.context;


import com.giffing.bucket4j.spring.boot.starter.context.properties.RateLimit;

/**
 * Used to check if the rate limit should be performed independently of the servlet|webflux|gateway request filter
 */
@FunctionalInterface
public interface RateLimitCheck<R> {

    /**
     * @param params        parameter information
     * @param mainRateLimit overwrites the rate limit configuration from the properties
     * @return null if no rate limit should be performed. (maybe skipped or shouldn't be executed).
     */
    RateLimitResultWrapper rateLimit(ExpressionParams<R> params, RateLimit mainRateLimit);

}
