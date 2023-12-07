package com.giffing.bucket4j.spring.boot.starter.config.filter.predicate;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;
import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicateDefinition;

class QueryExecutePredicateTest {

	private ExecutePredicate<Set<String>> predicate;

	@BeforeEach
	public void setup() {
		predicate = new QueryExecutePredicate<Set<String>>() {
			@Override
			public boolean test(Set<String> queryList) {
				return testQueryParameter(queryList);
			}
		};

	}
	
	@ParameterizedTest
	@MethodSource("validQueries")
	void assert_validQueries(QueryConfig queryConfig) {
		predicate.init(Map.of(ExecutePredicateDefinition.SIMPLE_CONFIG_KEY, queryConfig.config));
		Assertions.assertTrue(predicate.test(queryConfig.queryList));
	}
	
	@ParameterizedTest
	@MethodSource("invalidQeueries")
	void assert_invalidQueries(QueryConfig queryConfig) {
		predicate.init(Map.of(ExecutePredicateDefinition.SIMPLE_CONFIG_KEY, queryConfig.config));
		Assertions.assertFalse(predicate.test(queryConfig.queryList));
	}

	private record QueryConfig(String config, Set<String> queryList) {
		public static QueryConfig of(String config, String query) {
			return new QueryConfig(config, Set.of(query));
		}
		public static QueryConfig of(String config, Set<String> queries) {
			return new QueryConfig(config, queries);
		}
	}

	public static Stream<Arguments> validQueries() {
		return Stream.of(
				Arguments.of(QueryConfig.of("PARAM_1", "PARAM_1")),
				Arguments.of(QueryConfig.of("PARAM_3", Set.of("PARAM_1", "PARAM_2", "PARAM_3")))
				);
	}
	
	public static Stream<Arguments> invalidQeueries() {
		return Stream.of(
				Arguments.of(QueryConfig.of("PARAM_2", "PARAM_1")),
				Arguments.of(QueryConfig.of("PARAM_1", Set.of("PARAM_2", "PARAM_3")))
				);
	}

}
