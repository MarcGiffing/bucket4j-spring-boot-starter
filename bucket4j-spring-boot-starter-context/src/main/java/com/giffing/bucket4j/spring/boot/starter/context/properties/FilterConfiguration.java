package com.giffing.bucket4j.spring.boot.starter.context.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimitCheck;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitConditionMatchingStrategy;

/**
 * This class is the main configuration class which is used to build the servlet|webflux|gateway request filter
 *
 */
public class FilterConfiguration<R> {

	private RateLimitConditionMatchingStrategy strategy = RateLimitConditionMatchingStrategy.FIRST;
	
	/**
	 * The url on which the filter should listen.
	 */
	private String url;
	
	/**
	 * The order of the filter depending on other filters independently from the Bucket4j filters.
	 */
	private int order;
	
	/**
	 * Hides the HTTP response headers 
	 * x-rate-limit-remaining
	 * x-rate-limit-retry-after-seconds
	 */
	private Boolean hideHttpResponseHeaders = Boolean.FALSE;
	
	/**
	 * The HTTP Content-Type which should be returned when limiting the rate
	 */
	private String httpContentType;
	
	/**
	 * The HTTP response body which should be returned when limiting the rate.
	 */
	private String httpResponseBody;

	private Map<String, String> httpResponseHeaders = new HashMap<>();

	private List<RateLimitCheck<R>> rateLimitChecks = new ArrayList<>();
	
	private Metrics metrics;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public List<RateLimitCheck<R>> getRateLimitChecks() {
		return rateLimitChecks;
	}

	public void setRateLimitChecks(List<RateLimitCheck<R>> rateLimitChecks) {
		this.rateLimitChecks = rateLimitChecks;
	}

	public RateLimitConditionMatchingStrategy getStrategy() {
		return strategy;
	}

	public void setStrategy(RateLimitConditionMatchingStrategy strategy) {
		this.strategy = strategy;
	}

	public String getHttpResponseBody() {
		return httpResponseBody;
	}

	public void setHttpResponseBody(String httpResponseBody) {
		this.httpResponseBody = httpResponseBody;
	}

	public Map<String, String> getHttpResponseHeaders() {
		return httpResponseHeaders;
	}

	public void setHttpResponseHeaders(Map<String, String> httpResponseHeaders) {
		this.httpResponseHeaders = httpResponseHeaders;
	}

	public Metrics getMetrics() {
		return metrics;
	}

	public void setMetrics(Metrics metrics) {
		this.metrics = metrics;
	}

	public Boolean getHideHttpResponseHeaders() {
		return hideHttpResponseHeaders;
	}

	public void setHideHttpResponseHeaders(Boolean hideHttpResponseHeaders) {
		this.hideHttpResponseHeaders = hideHttpResponseHeaders;
	}

	public String getHttpContentType() {
		return httpContentType;
	}

	public void setHttpContentType(String httpContentType) {
		this.httpContentType = httpContentType;
	}

}
