package com.giffing.bucket4j.spring.boot.starter.examples.caffeine;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;

@Component
public class CustomQueryExecutePredicate extends ExecutePredicate<HttpServletRequest> {

	private String query;
	
	@Override
	public boolean test(HttpServletRequest t) {
		boolean result = t.getParameterMap().containsKey(query);
		System.out.printf("query-parameter;value:%s;result:%s%n", query, result);
		return result;
	}
	
	@Override
	public String name() {
		return "CUSTOM-QUERY";
	}

	@Override
	public void parseSimpleConfig(String simpleConfig) {
		this.query = simpleConfig;
	}

}
