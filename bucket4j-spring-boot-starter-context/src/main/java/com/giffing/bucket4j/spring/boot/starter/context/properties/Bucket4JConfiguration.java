package com.giffing.bucket4j.spring.boot.starter.context.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.micrometer.common.util.StringUtils;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.springframework.core.Ordered;

import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitConditionMatchingStrategy;

import lombok.Data;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Data
@ToString
public class Bucket4JConfiguration {

	/**
	 * The cache name. Should be provided or an exception is thrown
	 */
	@NotBlank
	private String cacheName = "buckets";
	
	/**
	 * The default {@link FilterMethod} is {@link FilterMethod#SERVLET}
	 */
	@NotNull
	private FilterMethod filterMethod = FilterMethod.SERVLET;
	
	/**
	 * The default strategy is {@link RateLimitConditionMatchingStrategy#FIRST}.
	 */
	@NotNull
	private RateLimitConditionMatchingStrategy strategy = RateLimitConditionMatchingStrategy.FIRST;
	
	/**
	 * The URL to which the filter should be registered
	 * 
	 */
	@NotBlank
	private String url = ".*";
	
	/**
	 * The filter order has a default of the highest precedence reduced by 10
	 */
	@NotNull
	private Integer filterOrder = Ordered.HIGHEST_PRECEDENCE + 10;

	@NotEmpty
	private List<RateLimit> rateLimits = new ArrayList<>();
	
	/**
	 * The HTTP Content-Type which should be returned
	 */
	@NotBlank
	private String httpContentType = "application/json";
	
	/**
	 * The HTTP status code which should be returned when limiting the rate.
	 */
	@NotNull
	private HttpStatus httpStatusCode = HttpStatus.TOO_MANY_REQUESTS;
	
	/**
	 * The HTTP content which should be used in case of rate limiting
	 */
	@NotBlank
	private String httpResponseBody = "{ \"message\": \"Too many requests!\" }";

	/**
	 * Hides the HTTP response headers 
	 * x-rate-limit-remaining
	 * x-rate-limit-retry-after-seconds
	 * 
	 * It does not effect custom defined httpResponseHeaders.
	 */
	@NotNull
	private Boolean hideHttpResponseHeaders = Boolean.FALSE;
	
	private Map<String, String> httpResponseHeaders = new HashMap<>();

	private Metrics metrics = new Metrics();

	private String id;

	public void setId(String id) {
		if(!StringUtils.isEmpty(id)){
			this.id = id;
		}
	}
}