package com.giffing.bucket4j.spring.boot.starter.servlet;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.giffing.bucket4j.spring.boot.starter.context.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitCheck;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitConditionMatchingStrategy;
import com.giffing.bucket4j.spring.boot.starter.servlet.ServletRequestFilter;
import com.netflix.zuul.context.RequestContext;

import io.github.bucket4j.ConsumptionProbe;

public class ServletRateLimitFilterTest {

	private ServletRequestFilter filter;
	private FilterConfiguration configuration;
	private RateLimitCheck rateLimitCheck1;
	private RateLimitCheck rateLimitCheck2;
	private RateLimitCheck rateLimitCheck3;

	private ConsumptionProbe consumptionProbe;
	
	@Before
    public void setup() {
		consumptionProbe = Mockito.mock(ConsumptionProbe.class);
		when(consumptionProbe.isConsumed()).thenReturn(true);
		
    	rateLimitCheck1 = mock(RateLimitCheck.class);
        rateLimitCheck2 = mock(RateLimitCheck.class);
        rateLimitCheck3 = mock(RateLimitCheck.class);
        
        configuration = new FilterConfiguration();
        configuration.setRateLimitChecks(Arrays.asList(rateLimitCheck1, rateLimitCheck2, rateLimitCheck3));
        configuration.setUrl("url");
        filter = new ServletRequestFilter(configuration);
    }
	
	@Test
	public void should_execute_all_checks_when_using_RateLimitConditionMatchingStrategy_All() throws Exception {
		
        when(rateLimitCheck1.rateLimit(any())).thenReturn(consumptionProbe);
        when(rateLimitCheck2.rateLimit(any())).thenReturn(consumptionProbe);
        when(rateLimitCheck3.rateLimit(any())).thenReturn(consumptionProbe);
        
        configuration.setStrategy(RateLimitConditionMatchingStrategy.ALL);
        
        standaloneSetup(new TestController())
			.addFilters(filter).build()
			.perform(get(("/test")));
        
        verify(rateLimitCheck1, times(1)).rateLimit(any());
        verify(rateLimitCheck2, times(1)).rateLimit(any());
        verify(rateLimitCheck3, times(1)).rateLimit(any());
	}
	
	@Test
	public void should_execute_first_check_when_using_RateLimitConditionMatchingStrategy_All_but_first_is_not_consumed() throws Exception {
		
        when(rateLimitCheck1.rateLimit(any())).thenReturn(consumptionProbe);
        when(rateLimitCheck2.rateLimit(any())).thenReturn(consumptionProbe);
        when(rateLimitCheck3.rateLimit(any())).thenReturn(consumptionProbe);
        
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
	public void should_execute_only_one_check_when_using_RateLimitConditionMatchingStrategy_FIRST() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/url");
        RequestContext context = new RequestContext();
        context.setRequest(request);
        RequestContext.testSetCurrentContext(context);
        
        configuration.setStrategy(RateLimitConditionMatchingStrategy.FIRST);

        when(rateLimitCheck1.rateLimit(any())).thenReturn(consumptionProbe);
        
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
