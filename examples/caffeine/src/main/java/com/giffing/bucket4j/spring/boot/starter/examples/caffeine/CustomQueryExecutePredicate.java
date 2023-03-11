package com.giffing.bucket4j.spring.boot.starter.examples.caffeine;

import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.config.servlet.predicate.ServletRequestExecutePredicate;
import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class CustomQueryExecutePredicate extends ServletRequestExecutePredicate {

	@Override
	public String name() {
		return "CUSTOM-QUERY";
	}

	@Override
	public boolean test(HttpServletRequest t) {
		boolean result = t.getParameterMap().containsKey(getValue());
		System.out.println("query-parametetr;value:%s;result:%s".formatted(getValue(), result));
		return result;
	}

	@Override
	public ExecutePredicate<HttpServletRequest> newInstance() {
		return new CustomQueryExecutePredicate();
	}

}
