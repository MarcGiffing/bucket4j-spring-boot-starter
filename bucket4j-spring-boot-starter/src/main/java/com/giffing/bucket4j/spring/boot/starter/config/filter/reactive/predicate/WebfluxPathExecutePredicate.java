package com.giffing.bucket4j.spring.boot.starter.config.filter.reactive.predicate;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.config.filter.predicate.PathExecutePredicate;

@Component
public class WebfluxPathExecutePredicate extends PathExecutePredicate<ServerHttpRequest>{

	@Override
	public boolean test(ServerHttpRequest t) {
		return testPath(t.getPath().value());
	}

}
