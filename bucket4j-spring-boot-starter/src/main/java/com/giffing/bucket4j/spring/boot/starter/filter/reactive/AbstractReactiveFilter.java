package com.giffing.bucket4j.spring.boot.starter.filter.reactive;

import com.giffing.bucket4j.spring.boot.starter.context.ExpressionParams;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitConditionMatchingStrategy;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitResult;
import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@Data
@Slf4j
public class AbstractReactiveFilter {

	private FilterConfiguration<ServerHttpRequest, ServerHttpResponse> filterConfig;

	public AbstractReactiveFilter(FilterConfiguration<ServerHttpRequest, ServerHttpResponse> filterConfig) {
		this.filterConfig = filterConfig;
	}

	public void setFilterConfig(FilterConfiguration<ServerHttpRequest, ServerHttpResponse> filterConfig){
		this.filterConfig = filterConfig;
	}

	protected boolean urlMatches(ServerHttpRequest request) {
		return request.getURI().getPath().matches(filterConfig.getUrl());
	}

	protected Mono<Void> chainWithRateLimitCheck(ServerWebExchange exchange, ReactiveFilterChain chain) {
		log.debug("reate-limit-check;method:{};uri:{}", exchange.getRequest().getMethod(), exchange.getRequest().getURI());
		var request = exchange.getRequest();
		var response = exchange.getResponse();
		List<Mono<RateLimitResult>> asyncConsumptionProbes = new ArrayList<>();
		for (var rlc : filterConfig.getRateLimitChecks()) {
			var wrapper = rlc.rateLimit(new ExpressionParams<>(request), null);
			if(wrapper != null && wrapper.getRateLimitResultCompletableFuture() != null){
				asyncConsumptionProbes.add(Mono.fromFuture(wrapper.getRateLimitResultCompletableFuture()));
				if(filterConfig.getStrategy() == RateLimitConditionMatchingStrategy.FIRST){
					break;
				}
			}
		}
		if(asyncConsumptionProbes.isEmpty()) {
			return chain.apply(exchange);
		}
		return Flux
				.concat(asyncConsumptionProbes)
				.reduce(this::reduceConsumptionProbe)
				.flatMap(rateLimitResult -> handleConsumptionProbe(exchange, chain, response, rateLimitResult));
	}

	protected RateLimitResult reduceConsumptionProbe(RateLimitResult x, RateLimitResult y) {
		RateLimitResult result;
		if(!x.isConsumed()) {
			result = x;
		} else if(!y.isConsumed()) {
			result = y;
		} else {
			result = x.getRemainingTokens() < y.getRemainingTokens() ? x : y;
		}
		log.debug("reduce-probes;result-isConsumed:{};result-getremainingTokens:{};x-isConsumed:{};x-getremainingTokens{};y-isConsumed:{};y-getremainingTokens{}",
				result.isConsumed(), result.getRemainingTokens(),
				x.isConsumed(), x.getRemainingTokens(),
				y.isConsumed(), y.getRemainingTokens());
		return result;
	}

	protected Mono<Void> handleConsumptionProbe(ServerWebExchange exchange, ReactiveFilterChain chain,
			ServerHttpResponse response, RateLimitResult rateLimitResult) {
		log.debug("probe-results;isConsumed:{};remainingTokens:{};nanosToWaitForRefill:{};nanosToWaitForReset:{}",
				rateLimitResult.isConsumed(),
				rateLimitResult.getRemainingTokens(),
				rateLimitResult.getNanosToWaitForRefill(),
				rateLimitResult.getNanosToWaitForReset());

		if (!rateLimitResult.isConsumed()) {
			if (Boolean.FALSE.equals(filterConfig.getHideHttpResponseHeaders())) {
				filterConfig.getHttpResponseHeaders().forEach(response.getHeaders()::addIfAbsent);
			}
			if (filterConfig.getHttpResponseBody() != null) {
				response.setStatusCode(filterConfig.getHttpStatusCode());
				response.getHeaders().set("Content-Type", filterConfig.getHttpContentType());
				DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(filterConfig.getHttpResponseBody().getBytes(UTF_8));
				return response.writeWith(Flux.just(buffer));
			} else {
				return Mono.error(new ReactiveRateLimitException(filterConfig.getHttpStatusCode(), null));
			}
		}
		if (Boolean.FALSE.equals(filterConfig.getHideHttpResponseHeaders())) {
			log.debug("header;X-Rate-Limit-Remaining:{}", rateLimitResult.getRemainingTokens());
			response.getHeaders().set("X-Rate-Limit-Remaining", String.valueOf(rateLimitResult.getRemainingTokens()));
		}

		Mono<Void> postRateLimitMonos = Mono.empty();
		filterConfig.getPostRateLimitChecks().forEach(rlc -> {
			var wrapper = rlc.rateLimit(exchange.getRequest(), response);
			if (wrapper != null && wrapper.getRateLimitResultCompletableFuture() != null) {
				postRateLimitMonos.and(Mono.fromFuture(wrapper.getRateLimitResultCompletableFuture()));
			}
		});

		return chain.apply(exchange).then(postRateLimitMonos);
	}
}
