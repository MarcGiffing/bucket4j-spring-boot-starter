package com.giffing.bucket4j.spring.boot.starter.examples.caffeine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;

import jakarta.servlet.http.HttpServletRequest;

@Component
@Slf4j
public class CustomQueryExecutePredicate extends ExecutePredicate<HttpServletRequest> {

	private String query;
	
	@Override
	public boolean test(HttpServletRequest t) {
		boolean result = t.getParameterMap().containsKey(query);
		log.info("query-parametetr;value:%s;result:%s".formatted(query, result));
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
