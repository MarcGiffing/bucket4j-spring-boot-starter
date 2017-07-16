package com.giffing.bucket4j.spring.boot.starter.context;

import javax.servlet.http.HttpServletRequest;

/**
 * The skip condition is used to skip a rate limit check. 
 */
@FunctionalInterface
public interface SkipCondition {
	
	/**
	 * 
	 * @param servletRequest e.g. to skip rate limit based on the IP address
	 * @return true if the rate limit check should be skipped
	 */
	boolean shouldSkip(HttpServletRequest servletRequest);
	
}
