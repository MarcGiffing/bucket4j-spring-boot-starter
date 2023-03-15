package com.giffing.bucket4j.spring.boot.starter.context;

import java.util.Map;
import java.util.function.Predicate;

/**
 * The ExecutePredicate is used to conditionally execute a rate limit.
 * 
 * @param <T> the type of the predicate
 */
public interface ExecutePredicate<T> extends Predicate<T> {

	/**
	 * The unique name of the ExecutionPredicate which can be used
	 * in the property configuration.
	 */
	String name();
	
	/**
	 * Initialize the ExecutionPredicate with the provided
	 * property configuration.
	 */
	ExecutePredicate<T> init(Map<String, String> args);

}
