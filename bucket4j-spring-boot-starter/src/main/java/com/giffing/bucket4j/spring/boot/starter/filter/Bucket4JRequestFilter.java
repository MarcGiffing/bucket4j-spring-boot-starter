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
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

public class Bucket4JRequestFilter extends OncePerRequestFilter {

	private Bucket4JFilterConfig filterConfig;
	
    public Bucket4JRequestFilter(Bucket4JFilterConfig filterConfig) {
    	this.filterConfig = filterConfig;
    }

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String key = filterConfig.getKeyFilter().key(httpRequest);
        System.out.println(key + " " + Thread.currentThread().getId());
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
	}
}
