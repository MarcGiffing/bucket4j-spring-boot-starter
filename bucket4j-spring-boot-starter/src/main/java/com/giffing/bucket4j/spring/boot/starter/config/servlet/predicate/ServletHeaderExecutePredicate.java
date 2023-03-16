package com.giffing.bucket4j.spring.boot.starter.config.servlet.predicate;

import java.util.Collections;

import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.predicates.HeaderExecutePredicate;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class ServletHeaderExecutePredicate extends HeaderExecutePredicate<HttpServletRequest>{

	@Override
	public boolean test(HttpServletRequest t) {
		var headerValues = Collections.list(t.getHeaders(getHeadername()))
							.stream()
							.toList();
		return testHeaderValues(headerValues);
	}

}
