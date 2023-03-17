package com.giffing.bucket4j.spring.boot.starter.config.filter.predicate;

import java.util.Set;

import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class QueryExecutePredicate<T> extends ExecutePredicate<T> {

	private String query;
	
	@Override
	public String name() {
		return "QUERY";
	}

	public boolean testQueryParameter(Set<String> queryParameters) {
		boolean result = queryParameters.contains(query);
		log.debug("query-parametetr;value:%s;result:%s".formatted(query, result));
		return result;
	}

	@Override
	public ExecutePredicate<T> parseSimpleConfig(String simpleConfig) {
		this.query = simpleConfig;
		return this;
	}

}
