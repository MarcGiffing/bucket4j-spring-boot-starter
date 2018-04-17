package com.giffing.bucket4j.spring.boot.starter.webflux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.giffing.bucket4j.spring.boot.starter.context.ConsumptionProbeHolder;
import com.giffing.bucket4j.spring.boot.starter.context.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitCheck;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitConditionMatchingStrategy;
import com.google.common.base.Functions;

import io.github.bucket4j.ConsumptionProbe;
import reactor.core.publisher.Mono;

public class WebfluxWebFilter implements WebFilter {

	private FilterConfiguration<ServerHttpRequest> filterConfig;

	public WebfluxWebFilter(FilterConfiguration<ServerHttpRequest> filterConfig) {
		this.filterConfig = filterConfig;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		ServerHttpResponse response = exchange.getResponse();
		if (request.getURI().getPath().matches(filterConfig.getUrl())) {
			
			List<CompletableFuture<Long>> rateLimitFutures = new ArrayList<>();
			
	        for (RateLimitCheck<ServerHttpRequest> rl : filterConfig.getRateLimitChecks()) {
				ConsumptionProbeHolder probeHolder = rl.rateLimit(request, true);
				if(probeHolder != null && probeHolder.getConsumptionProbeCompletableFuture() != null ) {
					
					CompletableFuture<ConsumptionProbe> limitCheckingFuture = probeHolder.getConsumptionProbeCompletableFuture();
					rateLimitFutures.add(limitCheckingFuture.thenCompose(probe -> {
						if(probe.isConsumed()) {
							return CompletableFuture.completedFuture(probe.getRemainingTokens());
						} else{	
							return CompletableFuture.completedFuture(null);
						}
			        }));
					
				}
				
			};
			
			CompletableFuture<Long> reduced = rateLimitFutures
				.stream()
				.reduce((CompletableFuture<Long>)null, (a1, b1) -> {
					if(a1 == null){
						return b1;
					}
					if(filterConfig.getStrategy().equals(RateLimitConditionMatchingStrategy.FIRST)) {
						return a1;
					}
					return a1.thenCombine(b1, (x,y) -> {
						if(x == null && y == null) {
							return null;
						}
						if(x != null && y == null) {
							return x;
						}
						if(x == null && y != null) {
							return y;
						}
						return x < y ? x : y;
						});
				});
			
			Long remainingLimit = null;
			if (reduced != null) {
				remainingLimit = reduced.join();
			}
			
			System.out.println("remaining: " + remainingLimit);
			if(remainingLimit == null || remainingLimit <= 0) {
            	throw new WebfluxRateLimitException(filterConfig.getHttpResponseBody());
            }
			if(remainingLimit != null) {
				response.getHeaders().set("X-Rate-Limit-Remaining", "" + remainingLimit);
			}
			return chain.filter(exchange);
			
	    }
		return Mono.never();
	}
	
	private long getRemainingLimit(Long remaining, ConsumptionProbe probe) {
		if(probe != null) {
			if(remaining == null) {
				remaining = probe.getRemainingTokens();
			} else if(probe.getRemainingTokens() < remaining) {
				remaining = probe.getRemainingTokens();
			}
		}
		return remaining;
	}
	
	

}
