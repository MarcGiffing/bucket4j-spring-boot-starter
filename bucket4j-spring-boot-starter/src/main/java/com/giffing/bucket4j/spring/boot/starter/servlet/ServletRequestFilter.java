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

import io.github.bucket4j.Bucket;
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
        
        boolean skipRateLimit = false;
        if (filterConfig.getSkipCondition() != null) {
        	skipRateLimit = filterConfig.getSkipCondition().shouldSkip(request);
        } 
        
        if(!skipRateLimit) {
        	String key = filterConfig.getKeyFilter().key(httpRequest);
        	Bucket bucket = filterConfig.getBuckets().getProxy(key, () -> filterConfig.getConfig());
        	
        	ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        	
        	if (probe.isConsumed()) {
        		httpResponse.setHeader("X-Rate-Limit-Remaining", "" + probe.getRemainingTokens());
        		filterChain.doFilter(httpRequest, httpResponse);
        	} else {
        		httpResponse.setStatus(429);
        		httpResponse.setHeader("X-Rate-Limit-Retry-After-Seconds", "" + TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));
        		httpResponse.setContentType("application/json");
        		httpResponse.getWriter().append("{ \"errorId\": 1023, \"message\": \"To many requests\"}");
        	}
        } else {
        	filterChain.doFilter(httpRequest, httpResponse);
        }
	}
}
