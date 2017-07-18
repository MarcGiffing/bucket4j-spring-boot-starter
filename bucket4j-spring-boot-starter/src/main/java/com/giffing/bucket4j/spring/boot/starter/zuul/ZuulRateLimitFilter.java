package com.giffing.bucket4j.spring.boot.starter.zuul;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import com.giffing.bucket4j.spring.boot.starter.RateLimitCheck;
import com.giffing.bucket4j.spring.boot.starter.context.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitConditionMatchingStrategy;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import io.github.bucket4j.ConsumptionProbe;

/**
 * {@link ZuulFilter} to configure Bucket4j on each request.
 */
public class ZuulRateLimitFilter extends ZuulFilter {

	private final Logger log = LoggerFactory.getLogger(ZuulRateLimitFilter.class);

	private FilterConfiguration filterConfig;

	public ZuulRateLimitFilter(FilterConfiguration filterConfig) {
		this.filterConfig = filterConfig;
	}

	@Override
	public Object run() {
		RequestContext context = RequestContext.getCurrentContext();
		HttpServletRequest request = context.getRequest();

        Long remainingLimit = null;
		for (RateLimitCheck rl : filterConfig.getRateLimitChecks()) {
			ConsumptionProbe probe = rl.rateLimit(request);
			if (probe != null) {
				if (probe.isConsumed()) {
					remainingLimit = getRemainingLimit(remainingLimit, probe);
				} else {
					context.setResponseStatusCode(HttpStatus.TOO_MANY_REQUESTS.value());
					context.setResponseBody(filterConfig.getHttpResponseBody());
					context.setSendZuulResponse(false);
				}
			}
			if(filterConfig.getStrategy().equals(RateLimitConditionMatchingStrategy.FIRST)) {
				break;
			}
		};

		return null;
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
