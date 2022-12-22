package com.giffing.bucket4j.spring.boot.starter.context.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.Ordered;

import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitConditionMatchingStrategy;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Bucket4JConfiguration {

	/**
	 * The cache name. Should be provided or an exception is thrown
	 */
	private String cacheName = "buckets";
	
	/**
	 * The default {@link FilterMethod} is {@link FilterMethod#SERVLET}
	 */
	private FilterMethod filterMethod = FilterMethod.SERVLET;
	
	/**
	 * The default strategy is {@link RateLimitConditionMatchingStrategy#FIRST}.
	 */
	private RateLimitConditionMatchingStrategy strategy = RateLimitConditionMatchingStrategy.FIRST;
	
	/**
	 * The URL to which the filter should be registered
	 * 
	 */
	private String url = ".*";
	
	/**
	 * The filter order has a default of the highest precedence reduced by 10
	 */
	private int filterOrder = Ordered.HIGHEST_PRECEDENCE + 10;

	private List<RateLimit> rateLimits = new ArrayList<>();
	
	private Metrics metrics = new Metrics();
	
	/**
	 * The HTTP Content-Type which should be returned
	 */
	private String httpContentType = "application/json";
	
	/**
	 * The HTTP content which should be used in case of rate limiting
	 */
	private String httpResponseBody = "{ \"message\": \"Too many requests!\" }";

	/**
	 * Hides the HTTP response headers 
	 * x-rate-limit-remaining
	 * x-rate-limit-retry-after-seconds
	 * 
	 * It does not effect custom defined httpResponseHeaders.
	 */
	private Boolean hideHttpResponseHeaders = Boolean.FALSE;
	
	private Map<String, String> httpResponseHeaders = new HashMap<>();
	
}