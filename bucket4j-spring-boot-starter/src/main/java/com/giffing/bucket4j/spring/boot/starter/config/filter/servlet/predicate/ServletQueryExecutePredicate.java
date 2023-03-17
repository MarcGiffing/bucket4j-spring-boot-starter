package com.giffing.bucket4j.spring.boot.starter.config.filter.servlet.predicate;

import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.config.filter.predicate.QueryExecutePredicate;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class ServletQueryExecutePredicate extends QueryExecutePredicate<HttpServletRequest> {

	@Override
	public boolean test(HttpServletRequest t) {
		return testQueryParameter(t.getParameterMap().keySet());
	}

}
