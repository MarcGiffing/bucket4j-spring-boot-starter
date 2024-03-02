package com.giffing.bucket4j.spring.boot.starter.context;



/**
 * Used to check if the rate limit should be performed independently from the servlet|webflux|gateway request filter 
 *
 */
@FunctionalInterface
public interface PostRateLimitCheck<R, P> {

	/**
	 * @param request the request information object
	 * @param response the response information object
	 * 
	 * @return null if no rate limit should be performed. (maybe skipped or shouldn't be executed).
	 */
	RateLimitResultWrapper rateLimit(R request, P response);
	
}
