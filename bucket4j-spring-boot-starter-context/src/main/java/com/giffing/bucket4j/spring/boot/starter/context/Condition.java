package com.giffing.bucket4j.spring.boot.starter.context;


/**
 * The skip condition is used to skip or execute a rate limit check. 
 */
@FunctionalInterface
public interface Condition<R> {
	
	/**
	 * 
	 * @param request e.g. to skip or execute rate limit based on the IP address
	 * @return true if the rate limit check should be skipped
	 */
	boolean evalute(R request);
	
}
