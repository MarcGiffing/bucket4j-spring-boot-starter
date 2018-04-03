package com.giffing.bucket4j.spring.boot.starter.context;

/**
 * Functional interface to retrieve the Bucket4j key. The key is used to identify the Bucket4j storage.  
 *
 */
@FunctionalInterface
public interface KeyFilter<R> {

	/**
	 * Return the unique Bucket4j storage key. You can think of the key as a unique identifier
	 * which is for example an IP-Address or a user name. The rate limit is then applied to each individual key. 
	 * 
	 * @param request HTTP request information of the current request
	 * @return the key to identify the the rate limit (IP, username, ...)
	 */
	String key(R request);
	
}
