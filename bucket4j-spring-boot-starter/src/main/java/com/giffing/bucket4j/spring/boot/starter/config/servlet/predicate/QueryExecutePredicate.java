package com.giffing.bucket4j.spring.boot.starter.config.servlet.predicate;

import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class QueryExecutePredicate extends ServletRequestExecutePredicate {

	private String query;
	
	@Override
	public String name() {
		return "QUERY";
	}

	@Override
	public boolean test(HttpServletRequest t) {
		boolean result = t.getParameterMap().containsKey(query);
		log.debug("query-parametetr;value:%s;result:%s".formatted(query, result));
		return result;
	}

	@Override
	public ExecutePredicate<HttpServletRequest> parseSimpleConfig(String simpleConfig) {
		this.query = simpleConfig;
		return this;
	}

}
