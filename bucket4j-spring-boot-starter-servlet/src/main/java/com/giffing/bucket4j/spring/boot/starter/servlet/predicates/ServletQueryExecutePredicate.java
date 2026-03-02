package com.giffing.bucket4j.spring.boot.starter.servlet.predicates;

import com.giffing.bucket4j.spring.boot.starter.core.predicates.QueryExecutePredicate;
import jakarta.servlet.http.HttpServletRequest;

public class ServletQueryExecutePredicate extends QueryExecutePredicate<HttpServletRequest> {

	@Override
	public boolean test(HttpServletRequest t) {
		return testQueryParameter(t.getParameterMap().keySet());
	}

}
