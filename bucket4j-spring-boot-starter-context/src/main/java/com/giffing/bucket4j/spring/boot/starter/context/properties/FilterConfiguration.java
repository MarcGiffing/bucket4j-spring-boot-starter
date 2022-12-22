package com.giffing.bucket4j.spring.boot.starter.context.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimitCheck;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitConditionMatchingStrategy;

import lombok.Data;
import lombok.ToString;

/**
 * This class is the main configuration class which is used to build the servlet|webflux|gateway request filter
 *
 */
@Data
@ToString
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

}
