package com.giffing.bucket4j.spring.boot.starter.servlet;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import com.giffing.bucket4j.spring.boot.starter.RateLimitCheck;
import com.giffing.bucket4j.spring.boot.starter.context.FilterConfiguration;

import io.github.bucket4j.ConsumptionProbe;

/**
 * Servlet {@link Filter} class to configure Bucket4j on each request. 
 */
public class ServletRequestFilter extends OncePerRequestFilter {

	private FilterConfiguration filterConfig;
	
    public ServletRequestFilter(FilterConfiguration filterConfig) {
    	this.filterConfig = filterConfig;
    }

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        boolean allConsumed = true;
        long remainingLimit = 0;
        for (RateLimitCheck rl : filterConfig.getRateLimitChecks()) {
			ConsumptionProbe probe = rl.rateLimit(request);
			if(probe != null) {
				if(probe.isConsumed()) {
					remainingLimit = getRemainingLimit(remainingLimit, probe);
				} else{	allConsumed = false;
					httpResponse.setStatus(429);
					httpResponse.setHeader("X-Rate-Limit-Retry-After-Seconds", "" + TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));
					httpResponse.setContentType("application/json");
					httpResponse.getWriter().append("{ \"errorId\": 1023, \"message\": \"To many requests\"}");
				}
			}
		};
		if(allConsumed) {
			httpResponse.setHeader("X-Rate-Limit-Remaining", "" + remainingLimit);
			filterChain.doFilter(httpRequest, httpResponse);
		}
        
	}

	private long getRemainingLimit(long remaining, ConsumptionProbe probe) {
		if(probe != null) {
			if(probe.getRemainingTokens() < remaining) {
				remaining = probe.getRemainingTokens();
			}
		}
		return remaining;
	}
}
