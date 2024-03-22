package com.giffing.bucket4j.spring.boot.starter.context;

/**
 * Functional interface to retrieve the Bucket4j key. The key is used to identify the Bucket4j storage.  
 */
@FunctionalInterface
public interface KeyFilter<R> {

	/**
	 * Return the unique Bucket4j storage key. You can think of the key as a unique identifier
	 * which is for example an IP-Address or a username. The rate limit is then applied to each individual key.
	 * 
	 * @param expressionParams the expression params
	 * @return the key to identify the rate limit (IP, username, ...)
	 */
	String key(ExpressionParams<R> expressionParams);
	
}
