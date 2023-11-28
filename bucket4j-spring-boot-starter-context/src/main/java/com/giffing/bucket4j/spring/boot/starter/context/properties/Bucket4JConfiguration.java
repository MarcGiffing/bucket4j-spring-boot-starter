package com.giffing.bucket4j.spring.boot.starter.context.properties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.*;

import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;

import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitConditionMatchingStrategy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micrometer.common.util.StringUtils;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Bucket4JConfiguration implements Serializable {

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

	/**
	 * This identifier is used for saving and retrieving configurations in the cache.
	 * Setting the id is mandatory when configuration caching is enabled.
	 */
	private String id;

	public void setId(String id) {
		if(!StringUtils.isBlank(id)){
			this.id = id.trim();
		}
	}

	/**
	 * This version number is intended to be managed by the application.properties and for configuration changes by external systems during runtime.
	 * The version is only relevant for filters that hava their identifier set, and defaults to 1 if not configured.
	 *
	 * The value is maxed at 92 million, since it is combined with the minor version into a single Long and cannot exceed the Long max value.
	 */
	@Min(1)
	@Max(92000000L)
	private long majorVersion = 1;

	/**
	 * This version number is intended for internal configuration updates during runtime, for example based on CPU-usage of the system,
	 * and usually does not need to be configured in the application.properties.
	 * The version is only relevant for filters that hava their identifier set, and defaults to 1 if not configured.
	 *
	 * The value is maxed at 99 billion, since it is combined with the major version into a single Long version and cannot exceed the Long max value.
	 */
	@Min(1)
	@Max(99999999999L)
	private long minorVersion = 1;

	@JsonIgnore
	public long getBucket4JVersionNumber (){
		return (majorVersion * 100000000000L) + minorVersion;
	}
}