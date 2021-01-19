package com.giffing.bucket4j.spring.boot.starter.context;

/**
 * The filter method defines which type of should be used. 
 *
 */
public enum FilterMethod {
	
	/**
	 * Servlet Request Filter
	 */
	SERVLET,
	
	/**
	 * ZuulRoute filter 
	 */
	ZUUL,
	
	/**
	 * Spring Boots 5 async WebFilter
	 */
	WEBFLUX,
	
	/**
	 * Spring Cloud Gateway GlobalFilter
	 */

	GATEWAY;
	
	
	
}
