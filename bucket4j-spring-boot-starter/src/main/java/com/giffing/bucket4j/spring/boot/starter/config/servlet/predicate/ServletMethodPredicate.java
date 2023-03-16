package com.giffing.bucket4j.spring.boot.starter.config.servlet.predicate;

import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.predicates.MethodExecutePredicate;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class ServletMethodPredicate extends MethodExecutePredicate<HttpServletRequest> {

	@Override
	public boolean test(HttpServletRequest t) {
		return testRequestMethod(t.getMethod());
	}

}
