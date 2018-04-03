package com.giffing.bucket4j.spring.boot.starter.webflux;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.giffing.bucket4j.spring.boot.starter.context.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitCheck;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitConditionMatchingStrategy;

import io.github.bucket4j.ConsumptionProbe;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Bucket4jWebFilter implements WebFilter {

	private FilterConfiguration<ServerHttpRequest> filterConfig;

	public Bucket4jWebFilter(FilterConfiguration<ServerHttpRequest> filterConfig) {
		this.filterConfig = filterConfig;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		ServerHttpResponse response = exchange.getResponse();
		if (request.getURI().getPath().matches(filterConfig.getUrl())) {
			
			boolean allConsumed = true;
	        Long remainingLimit = null;
	        
	        for (RateLimitCheck<ServerHttpRequest> rl : filterConfig.getRateLimitChecks()) {
				ConsumptionProbe probe = rl.rateLimit(request);
				if(probe != null) {
					if(probe.isConsumed()) {
						remainingLimit = getRemainingLimit(remainingLimit, probe);
					} else{	
						allConsumed = false;
						handleHttpResponseOnRateLimiting(response, probe);
						return Mono.empty();
					}
					if(filterConfig.getStrategy().equals(RateLimitConditionMatchingStrategy.FIRST)) {
						break;
					}
				}
				
			};
			if(allConsumed) {
				if(remainingLimit != null) {
					response.getHeaders().set("X-Rate-Limit-Remaining", "" + remainingLimit);
				}
				chain.filter(exchange);
			}
			
	    } 
	    return chain.filter(exchange);
	}
	
	private void handleHttpResponseOnRateLimiting(ServerHttpResponse httpResponse, ConsumptionProbe probe) {
		httpResponse.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
		httpResponse.getHeaders().add("X-Rate-Limit-Retry-After-Seconds", "" + TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));
//		httpResponse.setContentType("application/json");
//		httpResponse.writeWith(Response.)
		// TODO HOW TO SET THE BODY??
//		byte[] string = "asdf".getBytes();
//		DataBuffer buffer = httpResponse.bufferFactory().allocateBuffer(string.length);
//		buffer.write(string);
//		httpResponse.writeAndFlushWith(Flux.just(Flux.just(buffer)));
		;
	}
	
	private Mono<DataBuffer> createBuffer(String msg, ServerHttpResponse httpResponse) {
			DataBufferFactory bufferFactory = httpResponse.bufferFactory();
			return Mono.just(bufferFactory.wrap(msg.getBytes()));
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
