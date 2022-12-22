package com.giffing.bucket4j.spring.boot.starter.context;

public enum RefillSpeed {

	/**
	 * Greedily regenerates tokens
	 */
	GREEDY,
	
	/**
	 * Regenerates tokens in an interval manner
	 */
	INTERVAL,
	
}
