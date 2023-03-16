package com.giffing.bucket4j.spring.boot.starter.config.servlet.predicate;

import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.predicates.PathExecutePredicate;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class ServletPathExecutePredicate extends PathExecutePredicate<HttpServletRequest>{

	@Override
	public boolean test(HttpServletRequest t) {
		return testPath(t.getServletPath());
	}

}
