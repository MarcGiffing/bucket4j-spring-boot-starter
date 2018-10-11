package com.giffing.bucket4j.spring.boot.starter.zuul;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;

import com.giffing.bucket4j.spring.boot.starter.context.ConsumptionProbeHolder;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitCheck;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitConditionMatchingStrategy;
import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import io.github.bucket4j.ConsumptionProbe;

/**
 * {@link ZuulFilter} to configure Bucket4j on each request.
 */
public class ZuulRateLimitFilter extends ZuulFilter {

	private FilterConfiguration<HttpServletRequest> filterConfig;

	public ZuulRateLimitFilter(FilterConfiguration<HttpServletRequest> filterConfig) {
		this.filterConfig = filterConfig;
	}

	@Override
	public Object run() {
		RequestContext context = getCurrentRequestContext();
		HttpServletRequest request = context.getRequest();

        Long remainingLimit = null;
		for (RateLimitCheck<HttpServletRequest> rl : filterConfig.getRateLimitChecks()) {
			ConsumptionProbeHolder probeHolder = rl.rateLimit(request, false);
			if (probeHolder != null && probeHolder.getConsumptionProbe() != null) {
				ConsumptionProbe probe = probeHolder.getConsumptionProbe();
				if (probe.isConsumed()) {
					remainingLimit = getRemainingLimit(remainingLimit, probe);
				} else {
					context.setResponseStatusCode(HttpStatus.TOO_MANY_REQUESTS.value());
					context.addZuulResponseHeader("X-Rate-Limit-Retry-After-Seconds", "" + TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));
					context.setResponseBody(filterConfig.getHttpResponseBody());
					context.setSendZuulResponse(false);
					break;
				}
				if(filterConfig.getStrategy().equals(RateLimitConditionMatchingStrategy.FIRST)) {
					break;
				}
			}
		};

		return null;
	}
	
	protected RequestContext getCurrentRequestContext() {
		return RequestContext.getCurrentContext();
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
		return requestURI.matches(filterConfig.getUrl());
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
