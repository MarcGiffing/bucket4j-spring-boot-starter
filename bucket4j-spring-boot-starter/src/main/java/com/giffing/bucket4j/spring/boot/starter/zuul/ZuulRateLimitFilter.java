package com.giffing.bucket4j.spring.boot.starter.zuul;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import com.giffing.bucket4j.spring.boot.starter.context.FilterConfiguration;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

public class ZuulRateLimitFilter extends ZuulFilter {

	private final Logger log = LoggerFactory.getLogger(ZuulRateLimitFilter.class);
	
	private FilterConfiguration filterConfig;

	public ZuulRateLimitFilter(FilterConfiguration filterConfig){
		this.filterConfig = filterConfig;
	}
	
	@Override
	public Object run() {
		RequestContext context = RequestContext.getCurrentContext();
		HttpServletRequest request = context.getRequest();

		boolean skipRateLimit = false;
        if (filterConfig.getSkipCondition() != null) {
        	skipRateLimit = filterConfig.getSkipCondition().shouldSkip(request);
        }

        if(!skipRateLimit) {
			String key = filterConfig.getKeyFilter().key(request);
			Bucket bucket = filterConfig.getBuckets().getProxy(key, () -> filterConfig.getConfig());
	
			ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
	
			if (probe.isConsumed()) {
			} else {
				context.setResponseStatusCode(HttpStatus.TOO_MANY_REQUESTS.value());
				context.setResponseBody("{ \"message\": \"To many requests\"}");
				context.setSendZuulResponse(false);
			}
		}
		return null;
	}
	

	@Override
	public boolean shouldFilter() {
		RequestContext currentContext = RequestContext.getCurrentContext();
		HttpServletRequest request = currentContext.getRequest();
		String requestURI = request.getRequestURI();
		return requestURI.startsWith(filterConfig.getUrl());
	}

	@Override
	public int filterOrder() {
		return filterConfig.getOrder();
	}

	@Override
	public String filterType() {
		return "pre";
	}

}
