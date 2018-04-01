package com.giffing.bucket4j.spring.boot.starter.context;

import javax.servlet.http.HttpServletRequest;

import io.github.bucket4j.ConsumptionProbe;

/**
 * Used to check if the rate limit should be performed independently from the Servlet Filter or ZuulFilter.  
 *
 */
@FunctionalInterface
public interface RateLimitCheck {

	/**
	 * @param request the http servlet request of the current request
	 * 
	 * @return null if no rate limit should be performed. (maybe skipped or shouldn't be executed).
	 */
	ConsumptionProbe rateLimit(HttpServletRequest request);
	
}
