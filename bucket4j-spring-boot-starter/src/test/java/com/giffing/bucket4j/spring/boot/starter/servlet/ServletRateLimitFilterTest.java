package com.giffing.bucket4j.spring.boot.starter.servlet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.Arrays;
import java.util.Map;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimitResult;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitResultWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimitCheck;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitConditionMatchingStrategy;
import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.filter.servlet.ServletRequestFilter;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class) 
class ServletRateLimitFilterTest {

	private ServletRequestFilter filter;
	private FilterConfiguration<HttpServletRequest, HttpServletResponse> configuration;
	@Mock private RateLimitCheck<HttpServletRequest> rateLimitCheck1;
	@Mock private RateLimitCheck<HttpServletRequest> rateLimitCheck2;
	@Mock private RateLimitCheck<HttpServletRequest> rateLimitCheck3;

	
	@Mock private RateLimitResultWrapper rateLimitResultWrapper;
	@Mock private RateLimitResult rateLimitResult;
	
	@BeforeEach
    public void setup() {
		when(rateLimitResult.isConsumed()).thenReturn(true);
		when(rateLimitResultWrapper.getRateLimitResult()).thenReturn(rateLimitResult);
        
        configuration = new FilterConfiguration<>();
        configuration.setRateLimitChecks(Arrays.asList(rateLimitCheck1, rateLimitCheck2, rateLimitCheck3));
        configuration.setUrl(".*");
        configuration.setHttpResponseHeaders(Map.of());
        filter = new ServletRequestFilter(configuration);
    }
	
	@Test
	void should_execute_all_checks_when_using_RateLimitConditionMatchingStrategy_All() throws Exception {
		
        when(rateLimitCheck1.rateLimit(any())).thenReturn(rateLimitResultWrapper);
        when(rateLimitCheck2.rateLimit(any())).thenReturn(rateLimitResultWrapper);
        when(rateLimitCheck3.rateLimit(any())).thenReturn(rateLimitResultWrapper);
        
        configuration.setStrategy(RateLimitConditionMatchingStrategy.ALL);
        
        standaloneSetup(new TestController())
			.addFilters(filter).build()
			.perform(get(("/test")));
        
        verify(rateLimitCheck1, times(1)).rateLimit(any());
        verify(rateLimitCheck2, times(1)).rateLimit(any());
        verify(rateLimitCheck3, times(1)).rateLimit(any());
	}
	
	@Test
	void should_execute_first_check_when_using_RateLimitConditionMatchingStrategy_All_but_first_is_not_consumed() throws Exception {
		
        when(rateLimitCheck1.rateLimit(any())).thenReturn(rateLimitResultWrapper);
        
        when(rateLimitResult.isConsumed()).thenReturn(false);
        
        configuration.setStrategy(RateLimitConditionMatchingStrategy.ALL);
        
        standaloneSetup(new TestController())
			.addFilters(filter).build()
			.perform(get(("/test")));
        
        verify(rateLimitCheck1, times(1)).rateLimit(any());
        verify(rateLimitCheck2, times(0)).rateLimit(any());
        verify(rateLimitCheck3, times(0)).rateLimit(any());
	}

	@Test
	void should_execute_only_one_check_when_using_RateLimitConditionMatchingStrategy_FIRST() throws Exception {
        configuration.setStrategy(RateLimitConditionMatchingStrategy.FIRST);

        when(rateLimitCheck1.rateLimit(any())).thenReturn(rateLimitResultWrapper);
        
        standaloneSetup(new TestController())
			.addFilters(filter).build()
			.perform(get(("/test")));
        
        
        verify(rateLimitCheck1, times(1)).rateLimit(any());
        verify(rateLimitCheck2, times(0)).rateLimit(any());
        verify(rateLimitCheck3, times(0)).rateLimit(any());
	}
	
	
	@RestController
	private static class TestController {

		@GetMapping("/test")
		public String forward() {
			return "Hello World";
		}

	}
	
}
