package com.giffing.bucket4j.spring.boot.starter.filter.reactive;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimitConditionMatchingStrategy;
import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

SuppressWarnings
SuppressWarnings
public class AbstractReactiveFilter {
	
	private final FilterConfiguration<ServerHttpRequest> SuppressWarnings
	
	public AbstractReactiveFilter(FilterConfiguration<ServerHttpRequest> filterConfig) {
		this.filterConfig = filterConfig;
	}

	protected boolean urlMatches(ServerHttpRequest request) {
		return request.getURI().getPath().matches(filterConfig.getUrl());
	}
	
	protected Mono<Void> chainWithRateLimitCheck(ServerWebExchange exchange, ReactiveFilterChain chain) {
		log.debug("reate-limit-check;method:{};uri:{}", exchange.getRequest().getMethod(), exchange.getRequest().getURI());
		ServerHttpRequest request = exchange.getRequest();
		ServerHttpResponse response = exchange.getResponse();
		List<Mono<ConsumptionProbe>> asyncConsumptionProbes = filterConfig.getRateLimitChecks()
			.stream()
			.map(rl -> rl.rateLimit(request))
			.filter(cph -> cph != null && cph.getConsumptionProbeCompletableFuture() != null)
			.map(cph -> Mono.fromFuture(cph.getConsumptionProbeCompletableFuture()))
			.collect(Collectors.toList());
		
		AtomicInteger consumptionProbeCounter = new AtomicInteger(0);
		return Flux
			.concat(asyncConsumptionProbes)
			//.takeWhile(Objects::nonNull)
			.doOnNext(cp -> consumptionProbeCounter.incrementAndGet())
			.takeWhile(cp -> shouldTakeMoreConsumptionProbe(consumptionProbeCounter))
			.reduce(this::reduceConsumptionProbe)
			.flatMap(consumptionProbe -> handleConsumptionProbe(exchange, chain, response, consumptionProbe));
	}

	protected boolean shouldTakeMoreConsumptionProbe(AtomicInteger consumptionProbeCounter) {
		boolean shouldTakeMore = filterConfig.getStrategy().equals(RateLimitConditionMatchingStrategy.ALL) || (filterConfig.getStrategy().equals(RateLimitConditionMatchingStrategy.FIRST) && consumptionProbeCounter.get() == 1);
		log.debug("take-more-probes:{};probe-index:{};matching-strategy:{}", shouldTakeMore, consumptionProbeCounter.get(), filterConfig.getStrategy());
		return shouldTakeMore;
	}

	protected ConsumptionProbe reduceConsumptionProbe(ConsumptionProbe x, ConsumptionProbe y) {
		ConsumptionProbe result = null;
		if(!x.isConsumed()) {
			result = x;
		} else if(!y.isConsumed()) {
			result = y;
		} else {
			result = x.getRemainingTokens() < y.getRemainingTokens() ? x : y;	
		}
		log.debug("reduce-probes;result-isConsumed:{};result-getremainingTokens;x-isConsumed:{};x-getremainingTokens;y-isConsumed:{};y-getremainingTokens", 
				result.isConsumed(), result.getRemainingTokens(),
				x.isConsumed(), x.getRemainingTokens(),
				y.isConsumed(), y.getRemainingTokens());
		return result;
	}
	
	protected Mono<Void> handleConsumptionProbe(ServerWebExchange exchange, ReactiveFilterChain chain,
			ServerHttpResponse response, ConsumptionProbe cp) {
		log.debug("probe-results;isConsumed:{};remainingTokens:{};nanosToWaitForRefill:{};nanosToWaitForReset:{}", 
				cp.isConsumed(), 
				cp.getRemainingTokens(), 
				cp.getNanosToWaitForRefill(), 
				cp.getNanosToWaitForReset());
		
		if(!cp.isConsumed()) {
			if(Boolean.FALSE.equals(filterConfig.getHideHttpResponseHeaders())) {
				filterConfig.getHttpResponseHeaders().forEach(response.getHeaders()::addIfAbsent);	
			}
			if(filterConfig.getHttpResponseBody() != null) {
				response.setStatusCode(filterConfig.getHttpStatusCode());
				response.getHeaders().set("Content-Type", filterConfig.getHttpContentType());
				DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(filterConfig.getHttpResponseBody().getBytes(UTF_8));
				return response.writeWith(Flux.just(buffer));	
			} else {
				return Mono.error(new ReactiveRateLimitException(filterConfig.getHttpResponseBody()));
			}
		}
		if(Boolean.FALSE.equals(filterConfig.getHideHttpResponseHeaders())) {
			log.debug("header;X-Rate-Limit-Remaining:{}", cp.getRemainingTokens());
			response.getHeaders().set("X-Rate-Limit-Remaining", "" + cp.getRemainingTokens());
		}
		return chain.apply(exchange);
	}
	
	
}
