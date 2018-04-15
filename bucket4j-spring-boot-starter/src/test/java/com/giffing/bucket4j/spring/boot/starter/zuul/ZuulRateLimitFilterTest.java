package com.giffing.bucket4j.spring.boot.starter.zuul;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;

import com.giffing.bucket4j.spring.boot.starter.context.ConsumptionProbeHolder;
import com.giffing.bucket4j.spring.boot.starter.context.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitCheck;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitConditionMatchingStrategy;
import com.netflix.zuul.context.RequestContext;

import io.github.bucket4j.ConsumptionProbe;

public class ZuulRateLimitFilterTest {

	private ZuulRateLimitFilter filter;
	private FilterConfiguration configuration;
	private RateLimitCheck rateLimitCheck1;
	private RateLimitCheck rateLimitCheck2;
	private RateLimitCheck rateLimitCheck3;

	private ConsumptionProbeHolder consumptionProbeHolder;
	private ConsumptionProbe consumptionProbe;
	
	@Before
    public void setup() {
		consumptionProbeHolder = Mockito.mock(ConsumptionProbeHolder.class);
		consumptionProbe = Mockito.mock(ConsumptionProbe.class);
		
		when(consumptionProbe.isConsumed()).thenReturn(true);
		when(consumptionProbeHolder.getConsumptionProbe()).thenReturn(consumptionProbe);
		
    	rateLimitCheck1 = mock(RateLimitCheck.class);
        rateLimitCheck2 = mock(RateLimitCheck.class);
        rateLimitCheck3 = mock(RateLimitCheck.class);
        
        configuration = new FilterConfiguration();
        configuration.setRateLimitChecks(Arrays.asList(rateLimitCheck1, rateLimitCheck2, rateLimitCheck3));
        configuration.setUrl("url");
        filter = new ZuulRateLimitFilter(configuration) {

			@Override
			protected RequestContext getCurrentRequestContext() {
				return Mockito.mock(RequestContext.class);
			}
        	
        };
    }
	
	@Test
	public void should_execute_all_checks_when_using_RateLimitConditionMatchingStrategy_All() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/url");
        RequestContext context = new RequestContext();
        context.setRequest(request);
        RequestContext.testSetCurrentContext(context);
        
        when(rateLimitCheck1.rateLimit(any(), Mockito.anyBoolean())).thenReturn(consumptionProbeHolder);
        when(rateLimitCheck2.rateLimit(any(), Mockito.anyBoolean())).thenReturn(consumptionProbeHolder);
        when(rateLimitCheck3.rateLimit(any(), Mockito.anyBoolean())).thenReturn(consumptionProbeHolder);
        
        configuration.setStrategy(RateLimitConditionMatchingStrategy.ALL);
        
        filter.run();
        
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

        when(rateLimitCheck1.rateLimit(any(), Mockito.anyBoolean())).thenReturn(consumptionProbeHolder);
        
        filter.run();
        
        
        verify(rateLimitCheck1, times(1)).rateLimit(any(), Mockito.anyBoolean());
        verify(rateLimitCheck2, times(0)).rateLimit(any(), Mockito.anyBoolean());
        verify(rateLimitCheck3, times(0)).rateLimit(any(), Mockito.anyBoolean());
	}
	
}
