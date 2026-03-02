package com.giffing.bucket4j.spring.boot.starter.webflux.predicates;

import com.giffing.bucket4j.spring.boot.starter.core.predicates.QueryExecutePredicate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;


@Component
public class WebfluxQueryExecutePredicate extends QueryExecutePredicate<ServerHttpRequest> {

	@Override
	public boolean test(ServerHttpRequest t) {
		return testQueryParameter(t.getQueryParams().keySet());
	}

}
