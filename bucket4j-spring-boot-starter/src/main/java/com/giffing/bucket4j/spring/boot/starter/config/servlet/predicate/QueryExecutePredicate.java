package com.giffing.bucket4j.spring.boot.starter.config.servlet.predicate;

import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class QueryExecutePredicate extends ServletRequestExecutePredicate {

	@Override
	public String name() {
		return "QUERY";
	}

	@Override
	public boolean test(HttpServletRequest t) {
		boolean result = t.getParameterMap().containsKey(getValue());
		log.debug("query-parametetr;value:%s;result:%s".formatted(getValue(), result));
		return result;
	}

	@Override
	public ExecutePredicate<HttpServletRequest> newInstance() {
		return new QueryExecutePredicate();
	}

}
