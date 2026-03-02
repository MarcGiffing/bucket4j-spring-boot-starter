package com.giffing.bucket4j.spring.boot.starter.servlet.predicates;

import com.giffing.bucket4j.spring.boot.starter.core.predicates.PathExecutePredicate;
import jakarta.servlet.http.HttpServletRequest;

public class ServletPathExecutePredicate extends PathExecutePredicate<HttpServletRequest>{

	@Override
	public boolean test(HttpServletRequest t) {
		return testPath(t.getServletPath());
	}

}
