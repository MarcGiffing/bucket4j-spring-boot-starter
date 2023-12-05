package com.giffing.bucket4j.spring.boot.starter.config.filter.predicate;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;
import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicateDefinition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HeaderExecutePredicateTest {

	private ExecutePredicate<List<String>> predicate;

	@BeforeEach
	public void setup() {
		predicate = new HeaderExecutePredicate<>() {
			@Override
			public boolean test(List<String> t) {
				return testHeaderValues(t);
			}
		};

	}

	@ParameterizedTest
	@MethodSource("validHeaders")
	void assert_validHeaders(HeaderConfig headerConfig) {
		predicate.init(Map.of(ExecutePredicateDefinition.SIMPLE_CONFIG_KEY, headerConfig.config));
		Assertions.assertTrue(predicate.test(headerConfig.headerValue));
	}
	
	@ParameterizedTest
	@MethodSource("invalidHeaders")
	void assert_invalidHeaders(HeaderConfig headerConfig) {
		predicate.init(Map.of(ExecutePredicateDefinition.SIMPLE_CONFIG_KEY, headerConfig.config));
		Assertions.assertFalse(predicate.test(headerConfig.headerValue));
	}

	private record HeaderConfig(String config, List<String> headerValue) {
		public static HeaderConfig of(String config, String headerValue) {
			return new HeaderConfig(config, List.of(headerValue));
		}

		public static HeaderConfig of(String config, List<String> headerValues) {
			return new HeaderConfig(config, headerValues);
		}
	}

	public static Stream<Arguments> validHeaders() {
		return Stream.of(
					Arguments.of(HeaderConfig.of("X-ROLE,.*ADMIN.*", "ADMIN")),
					Arguments.of(HeaderConfig.of("X-ROLE,.*ADMIN.*", "Other_ADMIN_Other")),
					Arguments.of(HeaderConfig.of("X-ROLE,.*ADMIN.*", "ADMIN_Other")),
					Arguments.of(HeaderConfig.of("X-ROLE,.*ADMIN.*", "Other_ADMIN")),
					Arguments.of(HeaderConfig.of("X-ROLE", "NOT_RELEVANT_WITHOUT_REGEX_CHECK")),
					Arguments.of(HeaderConfig.of("X-ROLE,ADMIN.*", "ADMIN_asfd"))
				);
	}
	
	public static Stream<Arguments> invalidHeaders() {
		return Stream.of(
					Arguments.of(HeaderConfig.of("X-ROLE,.*ADMIN.*", "admin")),
					Arguments.of(HeaderConfig.of("X-ROLE,.*ADMIN.*", "ADmIN")),
					Arguments.of(HeaderConfig.of("X-ROLE,.*ADMIN.*", List.of())),
					Arguments.of(HeaderConfig.of("X-ROLE,ADMIN.*", "Other_ADMIN")),
					Arguments.of(HeaderConfig.of("X-ROLE,.*ADMIN", "ADMIN_asfd"))
				);
	}

}
