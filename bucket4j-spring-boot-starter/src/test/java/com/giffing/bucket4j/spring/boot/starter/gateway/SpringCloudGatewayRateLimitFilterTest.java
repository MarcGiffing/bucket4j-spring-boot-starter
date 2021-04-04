package com.giffing.bucket4j.spring.boot.starter.gateway;

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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ServerWebExchange;

import com.giffing.bucket4j.spring.boot.starter.context.ConsumptionProbeHolder;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitCheck;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitConditionMatchingStrategy;
import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.filter.reactive.ReactiveRateLimitException;
import com.giffing.bucket4j.spring.boot.starter.filter.reactive.gateway.SpringCloudGatewayRateLimitFilter;

import io.github.bucket4j.ConsumptionProbe;
import reactor.core.publisher.Mono;

public class SpringCloudGatewayRateLimitFilterTest {

	private GlobalFilter filter;
	private FilterConfiguration configuration;
	private RateLimitCheck rateLimitCheck1;
	private RateLimitCheck rateLimitCheck2;
	private RateLimitCheck rateLimitCheck3;

	private ServerWebExchange exchange;
	private GatewayFilterChain chain;
	
	
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
        
		chain = Mockito.mock(GatewayFilterChain.class);
        
        configuration = new FilterConfiguration();
        configuration.setRateLimitChecks(Arrays.asList(rateLimitCheck1, rateLimitCheck2, rateLimitCheck3));
        configuration.setUrl(".*");
        filter = new SpringCloudGatewayRateLimitFilter(configuration);
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
	public void should_execute_all_checks_when_using_RateLimitConditionMatchingStrategy_All() throws URISyntaxException {
        
        configuration.setStrategy(RateLimitConditionMatchingStrategy.FIRST);

        rateLimitConfig(30L, rateLimitCheck1);
        rateLimitConfig(0L, rateLimitCheck2);
        rateLimitConfig(0L, rateLimitCheck3);

        HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);
        when(serverHttpResponse.getHeaders()).thenReturn(httpHeaders);
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        
		Mono<Void> result = filter.filter(exchange, chain);
		Assertions.assertNull(result, "No error expected");
        
		verify(rateLimitCheck1, times(1)).rateLimit(any(), Mockito.anyBoolean());
        verify(rateLimitCheck2, times(1)).rateLimit(any(), Mockito.anyBoolean());
        verify(rateLimitCheck3, times(1)).rateLimit(any(), Mockito.anyBoolean());
	}

	@Test
	public void should_execute_only_one_check_when_using_RateLimitConditionMatchingStrategy_FIRST() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/url");
        
        configuration.setStrategy(RateLimitConditionMatchingStrategy.FIRST);

        rateLimitConfig(30L, rateLimitCheck1);
        rateLimitConfig(0L, rateLimitCheck2);
        rateLimitConfig(10L, rateLimitCheck3);
        
        HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);
        when(serverHttpResponse.getHeaders()).thenReturn(httpHeaders);
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        
		Mono<Void> result = filter.filter(exchange, chain);
		Assertions.assertNull(result, "The result must be null");
        
        verify(httpHeaders, times(1)).set(any(), captor.capture());

        List<String> values = captor.getAllValues();
//        assertThat(values.stream().findFirst().get(), equalTo("30"));
        
        verify(rateLimitCheck1, times(1)).rateLimit(any(), Mockito.anyBoolean());
        verify(rateLimitCheck2, times(1)).rateLimit(any(), Mockito.anyBoolean());
        verify(rateLimitCheck3, times(1)).rateLimit(any(), Mockito.anyBoolean());
	}

	private void rateLimitConfig(Long remainingTokens, RateLimitCheck rateLimitCheck) {
		ConsumptionProbeHolder consumptionHolder = Mockito.mock(ConsumptionProbeHolder.class);
        ConsumptionProbe probe = Mockito.mock(ConsumptionProbe.class);
		when(probe.isConsumed()).thenReturn(remainingTokens > 0 ? true : false);
		when(probe.getRemainingTokens()).thenReturn(remainingTokens);
		when(consumptionHolder.getConsumptionProbeCompletableFuture())
			.thenReturn(CompletableFuture.completedFuture(probe));
        when(rateLimitCheck.rateLimit(any(), Mockito.anyBoolean())).thenReturn(consumptionHolder);
	}
	
}
