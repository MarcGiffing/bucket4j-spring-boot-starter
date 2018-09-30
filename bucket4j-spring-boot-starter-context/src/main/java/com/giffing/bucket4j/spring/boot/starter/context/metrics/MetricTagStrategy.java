package com.giffing.bucket4j.spring.boot.starter.context.metrics;

public interface MetricTagStrategy<R> {

	/**
	 * 
	 * @param request
	 * @return
	 */
	MetricTagResult getTags(R request);
	
	/**
	 * The unique id of the strategy which can be used in the configuration
	 * 
	 * @return the unique strategy key
	 */
	String key();
	
	boolean supports(Object request);
	
}
