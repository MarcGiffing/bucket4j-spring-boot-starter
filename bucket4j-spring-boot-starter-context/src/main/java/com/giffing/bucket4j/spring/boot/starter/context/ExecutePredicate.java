package com.giffing.bucket4j.spring.boot.starter.context;

import java.util.Map;
import java.util.function.Predicate;

/**
 * The ExecutePredicate is used to conditionally execute a rate limit.
 * 
 * @param <T> the type of the predicate
 */
public abstract class ExecutePredicate<T> implements Predicate<T> {

	/**
	 * The unique name of the ExecutionPredicate which can be used
	 * in the property configuration.
	 */
	public abstract String name();
	
	/**
	 * Initialize the ExecutionPredicate with the provided
	 * property configuration.
	 */
	public ExecutePredicate<T> init(Map<String, String> args) {
		if(hasSimpleConfig(args)) {
			parseSimpleConfig(args.get(ExecutePredicateDefinition.SIMPLE_CONFIG_KEY));
		} else {
			parseConfig(args);
		}
		return this;
	}
	
	protected abstract ExecutePredicate<T> parseSimpleConfig(String simpleConfig);
	
	protected ExecutePredicate<T> parseConfig(Map<String, String> args) {
		throw new UnsupportedOperationException("The ServletRequestExecutionPredicate %s doesn't support arguments"
				.formatted(this.getClass().getSimpleName()));
	}

	private boolean hasSimpleConfig(Map<String, String> args) {
		return args.containsKey(ExecutePredicateDefinition.SIMPLE_CONFIG_KEY);
	}

}
