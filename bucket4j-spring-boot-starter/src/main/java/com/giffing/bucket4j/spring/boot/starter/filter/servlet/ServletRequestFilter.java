package com.giffing.bucket4j.spring.boot.starter.filter.servlet;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import com.giffing.bucket4j.spring.boot.starter.context.ConsumptionProbeHolder;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitCheck;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitConditionMatchingStrategy;
import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;

import io.github.bucket4j.ConsumptionProbe;

/**
 * Servlet {@link Filter} class to configure Bucket4j on each request. 
 */
public class ServletRequestFilter extends OncePerRequestFilter implements Ordered {

	private FilterConfiguration<HttpServletRequest> filterConfig;
	
    public ServletRequestFilter(FilterConfiguration<HttpServletRequest> filterConfig) {
    	this.filterConfig = filterConfig;
    }

	public void setFilterConfig(FilterConfiguration<HttpServletRequest> filterConfig){
		this.filterConfig = filterConfig;
	}
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		return !request.getRequestURI().matches(filterConfig.getUrl());
	}
    
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
        boolean allConsumed = true;
        Long remainingLimit = null;
        for (RateLimitCheck<HttpServletRequest> rl : filterConfig.getRateLimitChecks()) {
        	ConsumptionProbeHolder probeHolder = rl.rateLimit(request);
			if (probeHolder != null && probeHolder.getConsumptionProbe() != null) {
				ConsumptionProbe probe = probeHolder.getConsumptionProbe();
				if(probe.isConsumed()) {
					remainingLimit = getRemainingLimit(remainingLimit, probe);
				} else{	
					allConsumed = false;
					handleHttpResponseOnRateLimiting(response, probe);
					break;
				}
				if(filterConfig.getStrategy().equals(RateLimitConditionMatchingStrategy.FIRST)) {
					break;
				}
			}
			
		}
        
		if(allConsumed) {
			if(remainingLimit != null && Boolean.FALSE.equals(filterConfig.getHideHttpResponseHeaders())) {
				response.setHeader("X-Rate-Limit-Remaining", "" + remainingLimit);
			}
			filterChain.doFilter(request, response);
		}
        
	}

	private void handleHttpResponseOnRateLimiting(HttpServletResponse httpResponse, ConsumptionProbe probe) throws IOException {
		httpResponse.setStatus(filterConfig.getHttpStatusCode().value());
		if(Boolean.FALSE.equals(filterConfig.getHideHttpResponseHeaders())) {
			httpResponse.setHeader("X-Rate-Limit-Retry-After-Seconds", "" + TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));
			filterConfig.getHttpResponseHeaders().forEach(httpResponse::setHeader);
		}
		if(filterConfig.getHttpResponseBody() != null) {
			httpResponse.setContentType(filterConfig.getHttpContentType());
			httpResponse.getWriter().append(filterConfig.getHttpResponseBody());
		}
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
	public int getOrder() {
		return filterConfig.getOrder();
	}
}
