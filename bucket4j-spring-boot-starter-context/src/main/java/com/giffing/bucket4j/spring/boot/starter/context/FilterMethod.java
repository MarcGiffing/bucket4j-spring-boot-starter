package com.giffing.bucket4j.spring.boot.starter.context;

import javax.servlet.Filter;

/**
 * The filter method defines which type of should be used. 
 *
 */
public enum FilterMethod {
	
	/**
	 * Servlet Request {@link Filter}
	 */
	SERVLET,
	
	/**
	 * {@link ZuulRoute} filter 
	 */
	ZUUL;
	
}
