package com.giffing.bucket4j.spring.boot.starter.webflux;

import com.giffing.bucket4j.spring.boot.starter.context.*;
import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.filter.reactive.ReactiveRateLimitException;
import com.giffing.bucket4j.spring.boot.starter.filter.reactive.webflux.impl.DefaultWebfluxRateLimitFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WebfluxRateLimitFilterTest {

	private DefaultWebfluxRateLimitFilter filter;
	private FilterConfiguration<ServerHttpRequest, ServerHttpResponse> configuration;
	private RateLimitCheck<ServerHttpRequest> rateLimitCheck1;
	private RateLimitCheck<ServerHttpRequest> rateLimitCheck2;
	private RateLimitCheck<ServerHttpRequest> rateLimitCheck3;

	private PostRateLimitCheck<ServerHttpRequest, ServerHttpResponse> postRateLimitCheck;

	private ServerWebExchange exchange;
	private WebFilterChain chain;


	private ServerHttpResponse serverHttpResponse;

	@BeforeEach
	public void setup() throws URISyntaxException {
		rateLimitCheck1 = mock(RateLimitCheck.class);
		rateLimitCheck2 = mock(RateLimitCheck.class);
		rateLimitCheck3 = mock(RateLimitCheck.class);

		postRateLimitCheck = mock(PostRateLimitCheck.class);

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
		configuration.setPostRateLimitChecks(
				Collections.singletonList(
						postRateLimitCheck));
		configuration.setUrl(".*");
		filter = new DefaultWebfluxRateLimitFilter(configuration);
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

	@Test
	void should_execute_rate_limit_check_after_main_filter() {
		configuration.setStrategy(RateLimitConditionMatchingStrategy.ALL);

		rateLimitConfig(10L, rateLimitCheck1);
		rateLimitConfig(10L, rateLimitCheck2);
		rateLimitConfig(10L, rateLimitCheck3);

		var httpHeaders = Mockito.mock(HttpHeaders.class);
		when(serverHttpResponse.getHeaders()).thenReturn(httpHeaders);
		when(serverHttpResponse.getStatusCode()).thenReturn(HttpStatusCode.valueOf(200));

		when(chain.filter(exchange))
				.thenReturn(
						Mono.defer(() -> {
							when(serverHttpResponse.getStatusCode())
									.thenReturn(HttpStatusCode.valueOf(401));
							return Mono.empty();
						}));

		var statusCode =
                new AtomicReference<>(
                        serverHttpResponse.getStatusCode());
		configurePostRateLimit(
				postRateLimitCheck,
				Mono.defer(() -> {
							statusCode.set(
									serverHttpResponse.getStatusCode());
							return Mono.just(
									createRateLimitResult(0L));
						}
						));

		filter.filter(exchange, chain).block();

		Assertions.assertEquals(
				HttpStatusCode.valueOf(401),
				statusCode.get());
	}

	public static <T> Supplier<CompletableFuture<T>> createLazyFuture(Supplier<Mono<T>> monoSupplier) {
		return () -> monoSupplier.get().toFuture();
	}

	private void rateLimitConfig(Long remainingTokens, RateLimitCheck<ServerHttpRequest> rateLimitCheck) {
		RateLimitResultWrapper consumptionHolder = Mockito.mock(RateLimitResultWrapper.class);
		RateLimitResult rateLimitResult = createRateLimitResult(remainingTokens);
		when(consumptionHolder.getRateLimitResultCompletableFuture())
				.thenReturn(
						CompletableFuture.completedFuture(rateLimitResult));
		when(rateLimitCheck.rateLimit(any(), any())).thenReturn(consumptionHolder);
	}

	private void configurePostRateLimit(
			PostRateLimitCheck<ServerHttpRequest, ServerHttpResponse> rateLimitCheck,
			Long remainingTokens) {
		configurePostRateLimit(
				rateLimitCheck,
					Mono.just(
							createRateLimitResult(
									remainingTokens)));
	}

	private void configurePostRateLimit(
			PostRateLimitCheck<ServerHttpRequest, ServerHttpResponse> rateLimitCheck,
			Mono<RateLimitResult> result) {
		RateLimitResultWrapper consumptionHolder = Mockito.mock(RateLimitResultWrapper.class);

		when(consumptionHolder.getRateLimitResultCompletableFuture())
				.thenAnswer(invocation -> result.toFuture());
		when(rateLimitCheck.rateLimit(any(), any())).thenReturn(consumptionHolder);
	}

	private RateLimitResult createRateLimitResult(
			Long remainingTokens) {
		RateLimitResult rateLimitResult = Mockito.mock(RateLimitResult.class);
		when(rateLimitResult.isConsumed()).thenReturn(remainingTokens > 0);
		when(rateLimitResult.getRemainingTokens()).thenReturn(remainingTokens);

		return rateLimitResult;
	}

}
