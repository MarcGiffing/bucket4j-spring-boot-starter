package com.giffing.bucket4j.spring.boot.starter.servlet.predicates;

import com.giffing.bucket4j.spring.boot.starter.core.predicates.HeaderExecutePredicate;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;

public class ServletHeaderExecutePredicate extends HeaderExecutePredicate<HttpServletRequest>{

	@Override
	public boolean test(HttpServletRequest t) {
		var headerValues = Collections.list(t.getHeaders(getHeadername()))
							.stream()
							.toList();
		return testHeaderValues(headerValues);
	}

}
