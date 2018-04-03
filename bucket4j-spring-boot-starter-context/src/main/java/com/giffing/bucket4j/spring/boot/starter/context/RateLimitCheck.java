package com.giffing.bucket4j.spring.boot.starter.context;


import io.github.bucket4j.ConsumptionProbe;

/**
 * Used to check if the rate limit should be performed independently from the Servlet Filter or ZuulFilter.  
 *
 */
@FunctionalInterface
public interface RateLimitCheck<R> {

	/**
	 * @param request the request information object
	 * 
	 * @return null if no rate limit should be performed. (maybe skipped or shouldn't be executed).
	 */
	ConsumptionProbe rateLimit(R request);
	
}
