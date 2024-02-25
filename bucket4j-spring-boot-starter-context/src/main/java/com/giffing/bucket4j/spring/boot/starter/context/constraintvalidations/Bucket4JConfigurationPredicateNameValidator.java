package com.giffing.bucket4j.spring.boot.starter.context.constraintvalidations;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.http.server.reactive.ServerHttpRequest;

import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;
import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicateDefinition;
import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;


public class Bucket4JConfigurationPredicateNameValidator implements ConstraintValidator<ValidPredicateNames, Bucket4JConfiguration> {

	private final EnumMap<FilterMethod, Map<String, ExecutePredicate<?>>> filterPredicates = new EnumMap<>(FilterMethod.class);

	@Autowired
	public Bucket4JConfigurationPredicateNameValidator(List<ExecutePredicate<?>> executePredicates) {
		List<ExecutePredicate<?>> servletPredicates = new ArrayList<>();
		List<ExecutePredicate<?>> webfluxPredicates = new ArrayList<>();
		executePredicates.forEach(x -> {
			Class<?> genericType = GenericTypeResolver.resolveTypeArgument(x.getClass(), ExecutePredicate.class);
			if(genericType == null) return;
			if(genericType.getName().equals("jakarta.servlet.http.HttpServletRequest")){
				servletPredicates.add(x);
			} else if (genericType == ServerHttpRequest.class){
				webfluxPredicates.add(x);
			}
		});

		filterPredicates.put(FilterMethod.SERVLET, servletPredicates.stream()
				.collect(Collectors.toMap(ExecutePredicate::name, Function.identity())));
		filterPredicates.put(FilterMethod.WEBFLUX, webfluxPredicates.stream()
				.collect(Collectors.toMap(ExecutePredicate::name, Function.identity())));
	}

	@Override
	public boolean isValid(Bucket4JConfiguration configuration, ConstraintValidatorContext context) {
		Set<String> allExecutePredicateNames = configuration.getRateLimits().stream()
				.flatMap(r -> Stream.concat(r.getExecutePredicates().stream(), r.getSkipPredicates().stream()))
				.map(ExecutePredicateDefinition::getName)
				.collect(Collectors.toSet());

		String errorMessage = validateExecutePredicates(configuration, allExecutePredicateNames);

		if (errorMessage != null) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
			return false;
		}
		return true;
	}

	private String validateExecutePredicates(Bucket4JConfiguration configuration, Set<String> allExecutePredicateNames) {
		if (configuration.getFilterMethod() == FilterMethod.GATEWAY && !allExecutePredicateNames.isEmpty()) {
			return "Predicates are not supported for Gateway filters";
		}

		List<String> invalidPredicates = allExecutePredicateNames.stream()
				.filter(predicateName -> filterPredicates.get(configuration.getFilterMethod()).get(predicateName) == null)
				.toList();

		return invalidPredicates.isEmpty() ? null : buildErrorMessage(invalidPredicates);
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
