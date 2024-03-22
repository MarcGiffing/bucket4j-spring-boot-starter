package com.giffing.bucket4j.spring.boot.starter.context;


/**
 * This condition is used to skip or execute a rate limit check.
 */
@FunctionalInterface
public interface Condition<R> {
	
	/**
	 * 
	 * @param expressionParams parameters to evaluate the expression
	 * @return true if the rate limit check should be skipped
	 */
	boolean evaluate(ExpressionParams<R> expressionParams);
	
}
