package com.giffing.bucket4j.spring.boot.starter.context.properties;

import com.giffing.bucket4j.spring.boot.starter.context.PostRateLimitCheck;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitCheck;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitConditionMatchingStrategy;
import com.giffing.bucket4j.spring.boot.starter.context.UrlPatternMatcher;
import lombok.Data;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is the main configuration class which is used to build the servlet|webflux|gateway request filter
 *
 */
@Data
@ToString
public class FilterConfiguration<R, P> {

	private RateLimitConditionMatchingStrategy strategy = RateLimitConditionMatchingStrategy.FIRST;



	/**
	 * The url pattern on which the filter should listen.
	 */
	private String urlPattern;

	/**
	 * The UrlMatcher which is used to match the request url against the configured url pattern.
	 */
	private UrlPatternMatcher urlPatternMatcher;
	
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

	/**
	 * The HTTP status code which should be returned when limiting the rate.
	 */
	private HttpStatus httpStatusCode = HttpStatus.TOO_MANY_REQUESTS;
	
	private Map<String, String> httpResponseHeaders = new HashMap<>();

	private List<RateLimitCheck<R>> rateLimitChecks = new ArrayList<>();

	private List<PostRateLimitCheck<R, P>> postRateLimitChecks = new ArrayList<>();
	
	public void addRateLimitCheck(RateLimitCheck<R> rateLimitCheck) {
		this.rateLimitChecks.add(rateLimitCheck);
	}
	
	private Metrics metrics;

	public void addPostRateLimitCheck(PostRateLimitCheck<R, P> prlc) {
		getPostRateLimitChecks().add(prlc);
	}
}
