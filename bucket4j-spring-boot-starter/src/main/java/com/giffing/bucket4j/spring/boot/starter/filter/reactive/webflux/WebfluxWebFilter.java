package com.giffing.bucket4j.spring.boot.starter.filter.reactive.webflux;

import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.filter.reactive.AbstractReactiveFilter;

import reactor.core.publisher.Mono;

public class WebfluxWebFilter extends AbstractReactiveFilter implements WebFilter, Ordered {


	public WebfluxWebFilter(FilterConfiguration<ServerHttpRequest> filterConfig) {
		super(filterConfig);
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		if (urlMatches(request)) {
			return chainWithRateLimitCheck(exchange, chain::filter);
		}
		return chain.filter(exchange);
	}

	@Override
	public int getOrder() {
		return getFilterConfig().getOrder();
	}


}
