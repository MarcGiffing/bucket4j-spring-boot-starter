package com.giffing.bucket4j.spring.boot.starter.webflux.predicates;

import com.giffing.bucket4j.spring.boot.starter.core.predicates.HeaderExecutePredicate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;


@Component
public class WebfluxHeaderExecutePredicate extends HeaderExecutePredicate<ServerHttpRequest> {

	@Override
	public boolean test(ServerHttpRequest t) {
		return testHeaderValues(t.getHeaders().get(getHeadername()));
	}

}
