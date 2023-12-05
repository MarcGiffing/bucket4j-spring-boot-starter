package com.giffing.bucket4j.spring.boot.starter.config.filter.predicate;

import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;
import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicateDefinition;

class MethodExecutePredicateTest {

	private ExecutePredicate<String> predicate;

	@BeforeEach
	public void setup() {
		predicate = new MethodExecutePredicate<>() {
			@Override
			public boolean test(String trequestMethod) {
				return testRequestMethod(trequestMethod);
			}
		};

	}
	
	@ParameterizedTest
	@MethodSource("validMethods")
	void assert_validHeaders(MethodConfig methodConfig) {
		predicate.init(Map.of(ExecutePredicateDefinition.SIMPLE_CONFIG_KEY, methodConfig.config));
		Assertions.assertTrue(predicate.test(methodConfig.requestMethod));
	}
	
	@ParameterizedTest
	@MethodSource("invalidMethods")
	void assert_invalidMethods(MethodConfig methodConfig) {
		predicate.init(Map.of(ExecutePredicateDefinition.SIMPLE_CONFIG_KEY, methodConfig.config));
		Assertions.assertFalse(predicate.test(methodConfig.requestMethod));
	}

	private record MethodConfig(String config, String requestMethod) {
		public static MethodConfig of(String config, String requestMethod) {
			return new MethodConfig(config, requestMethod);
		}
	}

	public static Stream<Arguments> validMethods() {
		return Stream.of(
				Arguments.of(MethodConfig.of("GET", "GET")),
				Arguments.of(MethodConfig.of("POST", "POST")),
				Arguments.of(MethodConfig.of("PUT", "PUT")),
				Arguments.of(MethodConfig.of("GET,POST", "GET")),
				Arguments.of(MethodConfig.of("GET,POST", "POST")),
				Arguments.of(MethodConfig.of("PUT,GET,POST", "PUT")),
				Arguments.of(MethodConfig.of("PUT,POST", "POST"))
				);
	}
	
	public static Stream<Arguments> invalidMethods() {
		return Stream.of(
				Arguments.of(MethodConfig.of("GET", "POST")),
				Arguments.of(MethodConfig.of("POST", "GET")),
				Arguments.of(MethodConfig.of("PUT", "POST")),
				Arguments.of(MethodConfig.of("GET,POST", "PUT")),
				Arguments.of(MethodConfig.of("GET,POST", "GE")),
				Arguments.of(MethodConfig.of("PUT,GET,POST", "POS")),
				Arguments.of(MethodConfig.of("PUT,POST", "GET"))
				);
	}
	

}
