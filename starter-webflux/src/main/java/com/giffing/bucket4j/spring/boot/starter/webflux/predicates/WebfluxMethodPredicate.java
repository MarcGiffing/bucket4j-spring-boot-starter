package com.giffing.bucket4j.spring.boot.starter.webflux.predicates;

import com.giffing.bucket4j.spring.boot.starter.core.predicates.MethodExecutePredicate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;


@Component
public class WebfluxMethodPredicate extends MethodExecutePredicate<ServerHttpRequest> {

	@Override
	public boolean test(ServerHttpRequest t) {
		return testRequestMethod(t.getMethod().name());
	}

}
