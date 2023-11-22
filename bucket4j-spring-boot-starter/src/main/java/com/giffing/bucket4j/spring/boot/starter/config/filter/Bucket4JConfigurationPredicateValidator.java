package com.giffing.bucket4j.spring.boot.starter.config.filter;

import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;
import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicateDefinition;
import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.properties.RateLimit;
import com.giffing.bucket4j.spring.boot.starter.exception.ExecutePredicateBeanNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class Bucket4JConfigurationPredicateValidator {

	private final Map<FilterMethod, Map<String, ExecutePredicate>> filterPredicates = new HashMap<>();

	public Bucket4JConfigurationPredicateValidator(
			List<ExecutePredicate<HttpServletRequest>> servletPredicates,
			List<ExecutePredicate<ServerHttpRequest>> webfluxPredicates) {

		filterPredicates.put(FilterMethod.SERVLET, servletPredicates.stream()
				.collect(Collectors.toMap(ExecutePredicate::name, Function.identity())));
		filterPredicates.put(FilterMethod.WEBFLUX, webfluxPredicates.stream()
				.collect(Collectors.toMap(ExecutePredicate::name, Function.identity())));
	}

	public void validatePredicates(Bucket4JConfiguration config) throws ExecutePredicateBeanNotFoundException {
		var executePredicates = config
				.getRateLimits()
				.stream()
				.map(RateLimit::getExecutePredicates);
		var skipPredicates = config
				.getRateLimits()
				.stream()
				.map(RateLimit::getSkipPredicates);

		var allExecutePredicateNames = Stream.concat(executePredicates, skipPredicates)
				.flatMap(List::stream)
				.map(ExecutePredicateDefinition::getName)
				.collect(Collectors.toSet());
		allExecutePredicateNames.forEach(predicateName -> {
			if (filterPredicates.get(config.getFilterMethod()).get(predicateName) == null) {
				throw new ExecutePredicateBeanNotFoundException(predicateName);
			}
		});
	}

}
