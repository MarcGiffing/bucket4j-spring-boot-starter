package com.giffing.bucket4j.spring.boot.starter.webflux;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimitResult;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitResultWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimitCheck;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitConditionMatchingStrategy;
import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.filter.reactive.ReactiveRateLimitException;
import com.giffing.bucket4j.spring.boot.starter.filter.reactive.webflux.WebfluxWebFilter;

import io.github.bucket4j.ConsumptionProbe;
import reactor.core.publisher.Mono;

class WebfluxRateLimitFilterTest {

	private WebfluxWebFilter filter;
	private FilterConfiguration<ServerHttpRequest, ServerHttpResponse> configuration;
	private RateLimitCheck<ServerHttpRequest> rateLimitCheck1;
	private RateLimitCheck<ServerHttpRequest> rateLimitCheck2;
	private RateLimitCheck<ServerHttpRequest> rateLimitCheck3;

	private ServerWebExchange exchange;
	private WebFilterChain chain;


	private ServerHttpResponse serverHttpResponse;

	@BeforeEach
	public void setup() throws URISyntaxException {
		rateLimitCheck1 = mock(RateLimitCheck.class);
		rateLimitCheck2 = mock(RateLimitCheck.class);
		rateLimitCheck3 = mock(RateLimitCheck.class);

		exchange = Mockito.mock(ServerWebExchange.class);

		ServerHttpRequest serverHttpRequest = Mockito.mock(ServerHttpRequest.class);
		URI uri = new URI("url");
		when(serverHttpRequest.getURI()).thenReturn(uri);
		when(exchange.getRequest()).thenReturn(serverHttpRequest);

		serverHttpResponse = Mockito.mock(ServerHttpResponse.class);
		when(exchange.getResponse()).thenReturn(serverHttpResponse);

		chain = Mockito.mock(WebFilterChain.class);
		when(chain.filter(exchange)).thenReturn(Mono.empty());

		configuration = new FilterConfiguration<>();
		configuration.setRateLimitChecks(Arrays.asList(rateLimitCheck1, rateLimitCheck2, rateLimitCheck3));
		configuration.setUrl(".*");
		filter = new WebfluxWebFilter(configuration);
	}

	@Test void should_throw_rate_limit_exception_with_no_remaining_tokens() {

		configuration.setStrategy(RateLimitConditionMatchingStrategy.FIRST);

		rateLimitConfig(0L, rateLimitCheck1);
		HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);
		when(serverHttpResponse.getHeaders()).thenReturn(httpHeaders);

		AtomicBoolean hasRateLimitError = new AtomicBoolean(false);
		Mono<Void> result = filter.filter(exchange, chain)
				.onErrorResume(ReactiveRateLimitException.class, (e) -> {
					hasRateLimitError.set(true);
					return Mono.<Void>empty();
				});
		result.subscribe();
		Assertions.assertTrue(hasRateLimitError.get());
	}

	@Test
	void should_execute_all_checks_when_using_RateLimitConditionMatchingStrategy_All() throws URISyntaxException {

		configuration.setStrategy(RateLimitConditionMatchingStrategy.ALL);

		rateLimitConfig(30L, rateLimitCheck1);
		rateLimitConfig(0L, rateLimitCheck2);
		rateLimitConfig(0L, rateLimitCheck3);

		HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);
		when(serverHttpResponse.getHeaders()).thenReturn(httpHeaders);

		Assertions.assertThrows(ReactiveRateLimitException.class, () -> {
			Mono<Void> result = filter.filter(exchange, chain);
			result.block();
		});

		verify(rateLimitCheck1).rateLimit(any(), any());
		verify(rateLimitCheck2).rateLimit(any(), any());
		verify(rateLimitCheck3).rateLimit(any(), any());
	}

	@Test
	void should_execute_only_one_check_when_using_RateLimitConditionMatchingStrategy_FIRST() {

		configuration.setStrategy(RateLimitConditionMatchingStrategy.FIRST);

		rateLimitConfig(30L, rateLimitCheck1);
		rateLimitConfig(0L, rateLimitCheck2);
		rateLimitConfig(10L, rateLimitCheck3);

		HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);
		when(serverHttpResponse.getHeaders()).thenReturn(httpHeaders);

		Mono<Void> result = filter.filter(exchange, chain);
		result.block();

		final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(httpHeaders, times(1)).set(any(), captor.capture());

		List<String> values = captor.getAllValues();
		Assertions.assertEquals("30", values.stream().findFirst().get());

		verify(rateLimitCheck1, times(1)).rateLimit(any(), any());
		verify(rateLimitCheck2, times(0)).rateLimit(any(), any());
		verify(rateLimitCheck3, times(0)).rateLimit(any(), any());
	}

	private void rateLimitConfig(Long remainingTokens, RateLimitCheck<ServerHttpRequest> rateLimitCheck) {
		RateLimitResultWrapper consumptionHolder = Mockito.mock(RateLimitResultWrapper.class);
		RateLimitResult rateLimitResult = Mockito.mock(RateLimitResult.class);
		when(rateLimitResult.isConsumed()).thenReturn(remainingTokens > 0);
		when(rateLimitResult.getRemainingTokens()).thenReturn(remainingTokens);
		when(consumptionHolder.getRateLimitResultCompletableFuture())
				.thenReturn(CompletableFuture.completedFuture(rateLimitResult));
		when(rateLimitCheck.rateLimit(any(), any())).thenReturn(consumptionHolder);
	}

}
