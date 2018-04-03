package com.giffing.bucket4j.spring.boot.starter.servlet;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import com.giffing.bucket4j.spring.boot.starter.context.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitCheck;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitConditionMatchingStrategy;

import io.github.bucket4j.ConsumptionProbe;

/**
 * Servlet {@link Filter} class to configure Bucket4j on each request. 
 */
public class ServletRequestFilter extends OncePerRequestFilter {

	private FilterConfiguration<HttpServletRequest> filterConfig;
	
    public ServletRequestFilter(FilterConfiguration<HttpServletRequest> filterConfig) {
    	this.filterConfig = filterConfig;
    }

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        boolean allConsumed = true;
        Long remainingLimit = null;
        
        for (RateLimitCheck<HttpServletRequest> rl : filterConfig.getRateLimitChecks()) {
			ConsumptionProbe probe = rl.rateLimit(request);
			if(probe != null) {
				if(probe.isConsumed()) {
					remainingLimit = getRemainingLimit(remainingLimit, probe);
				} else{	
					allConsumed = false;
					handleHttpResponseOnRateLimiting(httpResponse, probe);
					break;
				}
				if(filterConfig.getStrategy().equals(RateLimitConditionMatchingStrategy.FIRST)) {
					break;
				}
			}
			
		};
		if(allConsumed) {
			if(remainingLimit != null) {
				httpResponse.setHeader("X-Rate-Limit-Remaining", "" + remainingLimit);
			}
			filterChain.doFilter(httpRequest, httpResponse);
		}
        
	}

	private void handleHttpResponseOnRateLimiting(HttpServletResponse httpResponse, ConsumptionProbe probe) throws IOException {
		httpResponse.setStatus(429);
		httpResponse.setHeader("X-Rate-Limit-Retry-After-Seconds", "" + TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));
		httpResponse.setContentType("application/json");
		httpResponse.getWriter().append(filterConfig.getHttpResponseBody());
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
}
