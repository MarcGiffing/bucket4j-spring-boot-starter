package com.giffing.bucket4j.spring.boot.starter.context.constraintvalidations;

import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;
import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicateDefinition;
import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Bucket4JConfigurationPredicateNameValidator implements ConstraintValidator<ValidPredicateNames, Bucket4JConfiguration> {

	private final Map<FilterMethod, Map<String, ExecutePredicate<?>>> filterPredicates = new HashMap<>();

	public Bucket4JConfigurationPredicateNameValidator(
			List<ExecutePredicate<HttpServletRequest>> servletPredicates,
			List<ExecutePredicate<ServerHttpRequest>> webfluxPredicates) {

		filterPredicates.put(FilterMethod.SERVLET, servletPredicates.stream()
				.collect(Collectors.toMap(ExecutePredicate::name, Function.identity())));
		filterPredicates.put(FilterMethod.WEBFLUX, webfluxPredicates.stream()
				.collect(Collectors.toMap(ExecutePredicate::name, Function.identity())));
	}

	@Override
	public boolean isValid(Bucket4JConfiguration configuration, ConstraintValidatorContext context) {
		List<String> invalidPredicates = getInvalidPredicates(configuration);
		if (!invalidPredicates.isEmpty()) {
			String errorMessage = buildErrorMessage(invalidPredicates);
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
		}
		return invalidPredicates.isEmpty();
	}

	public List<String> getInvalidPredicates(Bucket4JConfiguration config) {
		Set<String> allExecutePredicateNames = config.getRateLimits().stream()
				.flatMap(r -> Stream.concat(r.getExecutePredicates().stream(), r.getSkipPredicates().stream()))
				.map(ExecutePredicateDefinition::getName)
				.collect(Collectors.toSet());

		return allExecutePredicateNames.stream()
				.filter(predicateName -> filterPredicates.get(config.getFilterMethod()).get(predicateName) == null)
				.toList();
	}

	private String buildErrorMessage(List<String> invalidPredicates) {
		StringBuilder errorBuilder = new StringBuilder("Invalid predicate name");
		if (invalidPredicates.size() > 1) {
			errorBuilder.append("s");
		}
		errorBuilder.append(": ");
		errorBuilder.append(String.join(", ", invalidPredicates));
		return errorBuilder.toString();
	}
}
