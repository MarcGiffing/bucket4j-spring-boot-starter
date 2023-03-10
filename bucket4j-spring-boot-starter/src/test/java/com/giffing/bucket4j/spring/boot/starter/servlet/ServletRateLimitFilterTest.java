package com.giffing.bucket4j.spring.boot.starter.servlet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.giffing.bucket4j.spring.boot.starter.context.ConsumptionProbeHolder;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitCheck;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitConditionMatchingStrategy;
import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.filter.servlet.ServletRequestFilter;

import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class) 
class ServletRateLimitFilterTest {

	private ServletRequestFilter filter;
	private FilterConfiguration<HttpServletRequest> configuration;
	@Mock private RateLimitCheck<HttpServletRequest> rateLimitCheck1;
	@Mock private RateLimitCheck<HttpServletRequest> rateLimitCheck2;
	@Mock private RateLimitCheck<HttpServletRequest> rateLimitCheck3;

	
	@Mock private ConsumptionProbeHolder consumptionProbeHolder;
	@Mock private ConsumptionProbe consumptionProbe;
	
	@BeforeEach
    public void setup() {
		when(consumptionProbe.isConsumed()).thenReturn(true);
		when(consumptionProbeHolder.getConsumptionProbe()).thenReturn(consumptionProbe);
        
        configuration = new FilterConfiguration<>();
        configuration.setRateLimitChecks(Arrays.asList(rateLimitCheck1, rateLimitCheck2, rateLimitCheck3));
        configuration.setUrl(".*");
        configuration.setHttpResponseHeaders(Map.of());
        filter = new ServletRequestFilter(configuration);
    }
	
	@Test
	void should_execute_all_checks_when_using_RateLimitConditionMatchingStrategy_All() throws Exception {
		
        when(rateLimitCheck1.rateLimit(any())).thenReturn(consumptionProbeHolder);
        when(rateLimitCheck2.rateLimit(any())).thenReturn(consumptionProbeHolder);
        when(rateLimitCheck3.rateLimit(any())).thenReturn(consumptionProbeHolder);
        
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
		
        when(rateLimitCheck1.rateLimit(any())).thenReturn(consumptionProbeHolder);
        
        when(consumptionProbe.isConsumed()).thenReturn(false);
        
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

        when(rateLimitCheck1.rateLimit(any())).thenReturn(consumptionProbeHolder);
        
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
