package com.giffing.bucket4j.spring.boot.starter.exception;

/**
 * This exception should be thrown the the expression of an metric-tag is missing
 *
 */
public class MissingMetricTagExpressionException extends Bucket4jGeneralException {

	private static final long serialVersionUID = 1L;
	
	public MissingMetricTagExpressionException(String key) {
		super(key);
	}

}
