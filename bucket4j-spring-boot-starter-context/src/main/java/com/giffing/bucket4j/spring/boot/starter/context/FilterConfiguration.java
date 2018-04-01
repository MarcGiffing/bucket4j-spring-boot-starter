package com.giffing.bucket4j.spring.boot.starter.context;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is the main configuration class which is used to build the Servlet {@link Filter}s or {@link ZuulFilter}s.
 *
 */
public class FilterConfiguration {

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
	 * The HTTP response body which should be returned when limiting the rate.
	 */
	private String httpResponseBody;

	private List<RateLimitCheck> rateLimitChecks = new ArrayList<>();

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

	public List<RateLimitCheck> getRateLimitChecks() {
		return rateLimitChecks;
	}

	public void setRateLimitChecks(List<RateLimitCheck> rateLimitChecks) {
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

}
