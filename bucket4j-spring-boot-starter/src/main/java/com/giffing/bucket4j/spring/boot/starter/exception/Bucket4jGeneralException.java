package com.giffing.bucket4j.spring.boot.starter.exception;

import com.giffing.bucket4j.spring.boot.starter.config.failureanalyzer.Bucket4JAutoConfigFailureAnalyzer;

/**
 * All exceptions should be extend from the this base exception. 
 * The {@link Bucket4JAutoConfigFailureAnalyzer} uses this class as a base class to analyze
 * the exception on startup.
 *
 */
public abstract class Bucket4jGeneralException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	protected Bucket4jGeneralException() {
		super();
	}
	
	protected Bucket4jGeneralException(String message) {
		super(message);
	}

}
