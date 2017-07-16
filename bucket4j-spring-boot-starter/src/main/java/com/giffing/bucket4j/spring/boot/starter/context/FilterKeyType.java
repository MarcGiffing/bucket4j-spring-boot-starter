package com.giffing.bucket4j.spring.boot.starter.context;

import org.springframework.expression.Expression;

/**
 * Predefined filter type to identify the Bucket4j key which is used to
 * define to whom the rate limit should be applied. 
 *
 */
public enum FilterKeyType {
	
	/**
	 * No special filter is applied. All incoming uses the same rate limit key.
	 */
	DEFAULT,
	
	/**
	 * IP based filtering. The rate limit is based on each IP address. 
	 */
	IP,
	
	/**
	 * The expression type is used to define an individual expression based on the Spring Expression Language. 
	 */
	EXPRESSION;
	
}
