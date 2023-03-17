package com.giffing.bucket4j.spring.boot.starter.config.filter.reactive.predicate;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.config.filter.predicate.HeaderExecutePredicate;

@Component
public class WebfluxHeaderExecutePredicate extends HeaderExecutePredicate<ServerHttpRequest>{

	@Override
	public boolean test(ServerHttpRequest t) {
		return testHeaderValues(t.getHeaders().get(getHeadername()));
	}

}
