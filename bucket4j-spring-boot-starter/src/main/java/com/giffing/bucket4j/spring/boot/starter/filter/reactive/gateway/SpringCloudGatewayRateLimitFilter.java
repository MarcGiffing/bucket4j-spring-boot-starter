package com.giffing.bucket4j.spring.boot.starter.filter.reactive.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.filter.reactive.AbstractReactiveFilter;

import reactor.core.publisher.Mono;

import static java.util.Objects.nonNull;

/**
 * {@link GlobalFilter} to configure Bucket4j on each request.
 */
public class SpringCloudGatewayRateLimitFilter extends AbstractReactiveFilter implements GlobalFilter, Ordered {

	public SpringCloudGatewayRateLimitFilter(FilterConfiguration<ServerHttpRequest, ServerHttpResponse> filterConfig) {
		super(filterConfig);
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
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
