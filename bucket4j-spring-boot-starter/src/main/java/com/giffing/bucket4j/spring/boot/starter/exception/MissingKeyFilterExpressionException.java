package com.giffing.bucket4j.spring.boot.starter.exception;

/**
 * This exception should be thrown the the filter-key-type is set to expression but no
 * expression property was set.
 *
 */
public class MissingKeyFilterExpressionException extends Bucket4jGeneralException {

	private static final long serialVersionUID = 1L;

}
