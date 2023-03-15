package com.giffing.bucket4j.spring.boot.starter.examples.caffeine;

import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.config.servlet.predicate.ServletRequestExecutePredicate;
import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class CustomQueryExecutePredicate extends ServletRequestExecutePredicate {

	private String query;
	
	@Override
	public boolean test(HttpServletRequest t) {
		boolean result = t.getParameterMap().containsKey(query);
		System.out.println("query-parametetr;value:%s;result:%s".formatted(query, result));
		return result;
	}
	
	@Override
	public String name() {
		return "CUSTOM-QUERY";
	}

	@Override
	public ExecutePredicate<HttpServletRequest> parseSimpleConfig(String simpleConfig) {
		this.query = simpleConfig;
		return this;
	}

}
