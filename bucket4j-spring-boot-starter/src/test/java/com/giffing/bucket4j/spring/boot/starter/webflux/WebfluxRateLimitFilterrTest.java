package com.giffing.bucket4j.spring.boot.starter.webflux;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.fail;
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

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import com.giffing.bucket4j.spring.boot.starter.context.ConsumptionProbeHolder;
import com.giffing.bucket4j.spring.boot.starter.context.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitCheck;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitConditionMatchingStrategy;
import com.netflix.zuul.context.RequestContext;

import io.github.bucket4j.ConsumptionProbe;

public class WebfluxRateLimitFilterrTest {

	private WebfluxWebFilter filter;
	private FilterConfiguration configuration;
	private RateLimitCheck rateLimitCheck1;
	private RateLimitCheck rateLimitCheck2;
	private RateLimitCheck rateLimitCheck3;

	private ServerWebExchange exchange;
	private WebFilterChain chain;
	
	
	private ServerHttpResponse serverHttpResponse;
	
	@Before
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
        
        configuration = new FilterConfiguration();
        configuration.setRateLimitChecks(Arrays.asList(rateLimitCheck1, rateLimitCheck2, rateLimitCheck3));
        configuration.setUrl("url");
        filter = new WebfluxWebFilter(configuration);
    }
	
	@Test
	public void should_execute_all_checks_when_using_RateLimitConditionMatchingStrategy_All() throws URISyntaxException {
        
        configuration.setStrategy(RateLimitConditionMatchingStrategy.ALL);

        ConsumptionProbeHolder consumptionProbeHolder1 = Mockito.mock(ConsumptionProbeHolder.class);
        ConsumptionProbe consumptionProbe1 = Mockito.mock(ConsumptionProbe.class);
		when(consumptionProbe1.isConsumed()).thenReturn(true);
		when(consumptionProbe1.getRemainingTokens()).thenReturn(30L);
		when(consumptionProbeHolder1.getConsumptionProbeCompletableFuture()).thenReturn(CompletableFuture.completedFuture(consumptionProbe1));
        when(rateLimitCheck1.rateLimit(any(), Mockito.anyBoolean())).thenReturn(consumptionProbeHolder1);

        ConsumptionProbeHolder consumptionProbeHolder2 = Mockito.mock(ConsumptionProbeHolder.class);
        ConsumptionProbe consumptionProbe2 = Mockito.mock(ConsumptionProbe.class);
		when(consumptionProbe2.isConsumed()).thenReturn(true);
		when(consumptionProbe2.getRemainingTokens()).thenReturn(20L);
		when(consumptionProbeHolder2.getConsumptionProbeCompletableFuture()).thenReturn(CompletableFuture.completedFuture(consumptionProbe2));
        when(rateLimitCheck2.rateLimit(any(), Mockito.anyBoolean())).thenReturn(consumptionProbeHolder2);
        
        ConsumptionProbeHolder consumptionProbeHolder3 = Mockito.mock(ConsumptionProbeHolder.class);
        ConsumptionProbe consumptionProbe3 = Mockito.mock(ConsumptionProbe.class);
		when(consumptionProbe3.isConsumed()).thenReturn(true);
		when(consumptionProbe3.getRemainingTokens()).thenReturn(10L);
		when(consumptionProbeHolder3.getConsumptionProbeCompletableFuture()).thenReturn(CompletableFuture.completedFuture(consumptionProbe3));
        when(rateLimitCheck3.rateLimit(any(), Mockito.anyBoolean())).thenReturn(consumptionProbeHolder3);
        
		try {
			filter.filter(exchange, chain);
			fail("WebfluxRateLimitException expected");
		} catch(WebfluxRateLimitException e) {
			// expected exception
		} catch(Exception e) {
			fail("WebfluxRateLimitException expected");
		}
        
		verify(rateLimitCheck1, times(1)).rateLimit(any(), Mockito.anyBoolean());
        verify(rateLimitCheck2, times(1)).rateLimit(any(), Mockito.anyBoolean());
        verify(rateLimitCheck3, times(1)).rateLimit(any(), Mockito.anyBoolean());
	}

	@Test
	public void should_execute_only_one_check_when_using_RateLimitConditionMatchingStrategy_FIRST() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/url");
        RequestContext context = new RequestContext();
        context.setRequest(request);
        RequestContext.testSetCurrentContext(context);
        
        configuration.setStrategy(RateLimitConditionMatchingStrategy.FIRST);

        ConsumptionProbeHolder consumptionProbeHolder1 = Mockito.mock(ConsumptionProbeHolder.class);
        ConsumptionProbe consumptionProbe1 = Mockito.mock(ConsumptionProbe.class);
		when(consumptionProbe1.isConsumed()).thenReturn(true);
		when(consumptionProbe1.getRemainingTokens()).thenReturn(10L);
		when(consumptionProbeHolder1.getConsumptionProbeCompletableFuture()).thenReturn(CompletableFuture.completedFuture(consumptionProbe1));
        when(rateLimitCheck1.rateLimit(any(), Mockito.anyBoolean())).thenReturn(consumptionProbeHolder1);

        ConsumptionProbeHolder consumptionProbeHolder2 = Mockito.mock(ConsumptionProbeHolder.class);
        ConsumptionProbe consumptionProbe2 = Mockito.mock(ConsumptionProbe.class);
		when(consumptionProbe2.isConsumed()).thenReturn(true);
		when(consumptionProbe2.getRemainingTokens()).thenReturn(0L);
		when(consumptionProbeHolder2.getConsumptionProbeCompletableFuture()).thenReturn(CompletableFuture.completedFuture(consumptionProbe2));
        when(rateLimitCheck2.rateLimit(any(), Mockito.anyBoolean())).thenReturn(consumptionProbeHolder2);
        
        ConsumptionProbeHolder consumptionProbeHolder3 = Mockito.mock(ConsumptionProbeHolder.class);
        ConsumptionProbe consumptionProbe3 = Mockito.mock(ConsumptionProbe.class);
		when(consumptionProbe3.isConsumed()).thenReturn(true);
		when(consumptionProbe3.getRemainingTokens()).thenReturn(0L);
		when(consumptionProbeHolder3.getConsumptionProbeCompletableFuture()).thenReturn(CompletableFuture.completedFuture(consumptionProbe3));
        when(rateLimitCheck3.rateLimit(any(), Mockito.anyBoolean())).thenReturn(consumptionProbeHolder3);

        HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);
        when(serverHttpResponse.getHeaders()).thenReturn(httpHeaders);
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);


        
        try {
			filter.filter(exchange, chain );
		} catch(Exception e) {
			System.out.println(e.getMessage());
			fail("WebfluxRateLimitException expected");
		}
        
        verify(httpHeaders, times(1)).set(any(), captor.capture());

        List<String> values = captor.getAllValues();
        // TODO check only limit of first
        values.stream().forEach(v -> System.out.println(v));
        
        verify(rateLimitCheck1, times(1)).rateLimit(any(), Mockito.anyBoolean());
        verify(rateLimitCheck2, times(1)).rateLimit(any(), Mockito.anyBoolean());
        verify(rateLimitCheck3, times(1)).rateLimit(any(), Mockito.anyBoolean());
	}
	
}
