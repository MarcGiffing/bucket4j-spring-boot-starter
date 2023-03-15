package com.giffing.bucket4j.spring.boot.starter.config.servlet.predicate;

import java.util.Map;

import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;
import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicateDefinition;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class ServletRequestExecutePredicate implements ExecutePredicate<HttpServletRequest> {

	@Override
	public ExecutePredicate<HttpServletRequest> init(Map<String, String> args) {
		if(hasSimpleConfig(args)) {
			parseSimpleConfig(args.get(ExecutePredicateDefinition.SIMPLE_CONFIG_KEY));
		} else {
			parseConfig(args);
		}
		return this;
	}
	
	protected abstract ExecutePredicate<HttpServletRequest> parseSimpleConfig(String simpleConfig);
	
	protected ExecutePredicate<HttpServletRequest> parseConfig(Map<String, String> args) {
		throw new UnsupportedOperationException("The ServletRequestExecutionPredicate %s doesn't support arguments"
				.formatted(this.getClass().getSimpleName()));
	}

	private boolean hasSimpleConfig(Map<String, String> args) {
		return args.containsKey(ExecutePredicateDefinition.SIMPLE_CONFIG_KEY);
	}
	
}
