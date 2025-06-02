package com.giffing.bucket4j.spring.boot.starter.filter.reactive.webflux;

import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.filter.reactive.AbstractReactiveFilter;

import reactor.core.publisher.Mono;

import static java.util.Objects.nonNull;

public class WebfluxWebFilter extends AbstractReactiveFilter implements WebFilter, Ordered {


	public WebfluxWebFilter(FilterConfiguration<ServerHttpRequest, ServerHttpResponse> filterConfig) {
		super(filterConfig);
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		var variables =
				urlMatchAndExtract(exchange.getRequest());
		if (nonNull(variables)) {
			exchange.getAttributes().put(ATTRIBUTE_URL_VARIABLES, variables);
			return chainWithRateLimitCheck(exchange, chain::filter);
		}
		return chain.filter(exchange);
	}

	@Override
	public int getOrder() {
		return getFilterConfig().getOrder();
	}


}
