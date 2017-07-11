package com.giffing.bucket4j.spring.boot.starter.filter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.GenericFilterBean;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

public class Bucket4JRequestFilter extends GenericFilterBean {

	private Bucket4JFilterConfig filterConfig;
	
    public Bucket4JRequestFilter(Bucket4JFilterConfig filterConfig) {
    	this.filterConfig = filterConfig;
    }

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        
        Bucket bucket = filterConfig.getBuckets().getProxy(filterConfig.getKeyFilter().key(httpRequest), () -> filterConfig.getConfig());

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            httpResponse.setHeader("X-Rate-Limit-Remaining", "" + probe.getRemainingTokens());
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
             httpResponse.setStatus(429);
             httpResponse.setHeader("X-Rate-Limit-Retry-After-Seconds", "" + TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));
             httpResponse.setContentType("application/json");
             httpResponse.getWriter().append("{ \"errorId\": 1023, \"message\": \"To many requests\"}");
        }
		
	}


}
