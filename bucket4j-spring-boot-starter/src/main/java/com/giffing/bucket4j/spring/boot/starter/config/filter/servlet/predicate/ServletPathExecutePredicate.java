package com.giffing.bucket4j.spring.boot.starter.config.filter.servlet.predicate;

import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.config.filter.predicate.PathExecutePredicate;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class ServletPathExecutePredicate extends PathExecutePredicate<HttpServletRequest>{

	@Override
	public boolean test(HttpServletRequest t) {
		return testPath(t.getServletPath());
	}

}
