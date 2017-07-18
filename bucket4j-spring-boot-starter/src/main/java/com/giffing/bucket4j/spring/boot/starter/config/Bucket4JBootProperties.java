package com.giffing.bucket4j.spring.boot.starter.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.giffing.bucket4j.spring.boot.starter.context.Bucket4JBandWidth;
import com.giffing.bucket4j.spring.boot.starter.context.FilterKeyType;
import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitConditionMatchingStrategy;

@ConfigurationProperties(prefix = Bucket4JBootProperties.PROPERTY_PREFIX)
public class Bucket4JBootProperties {

	public static final String PROPERTY_PREFIX = "bucket4j";
	
	/**
	 * Enables or disables the Bucket4j Spring Boot Starter.
	 */
	private Boolean enabled = true;
	
	private List<Bucket4JConfiguration> filters = new ArrayList<>();
	
	public static class Bucket4JConfiguration {

		/**
		 * The cache name. Should be provided or an exception is thrown
		 */
		private String cacheName = "buckets";
		
		private FilterMethod filterMethod = FilterMethod.SERVLET;
		
		private RateLimitConditionMatchingStrategy strategy = RateLimitConditionMatchingStrategy.FIRST;
		
		/**
		 * Url to which the filter should be registered
		 */
		private String url = "/*";
		
		private int filterOrder = Integer.MIN_VALUE + 1;

		private List<RateLimit> rateLimits = new ArrayList<>();
		
		/**
		 * The HTTP content which should be used in case of rate limiting
		 */
		private String httpResponseBody = "{ \"message\": \"Too many requests!\" }";
		
		
		public Bucket4JConfiguration() {
		}
		
		public String getCacheName() {
			return cacheName;
		}

		public void setCacheName(String cacheName) {
			this.cacheName = cacheName;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public int getFilterOrder() {
			return filterOrder;
		}

		public void setFilterOrder(int filterOrder) {
			this.filterOrder = filterOrder;
		}

		public FilterMethod getFilterMethod() {
			return filterMethod;
		}

		public void setFilterMethod(FilterMethod filterMethod) {
			this.filterMethod = filterMethod;
		}

		public List<RateLimit> getRateLimits() {
			return rateLimits;
		}

		public void setRateLimits(List<RateLimit> rateLimits) {
			this.rateLimits = rateLimits;
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
	
	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public static String getPropertyPrefix() {
		return PROPERTY_PREFIX;
	}

	public List<Bucket4JConfiguration> getFilters() {
		return filters;
	}

	public void setFilters(List<Bucket4JConfiguration> filters) {
		this.filters = filters;
	}

	public static class RateLimit {
		
		private FilterKeyType filterKeyType = FilterKeyType.DEFAULT;
		
		/**
		 * SpEl condition to check if the rate-limit should apply. If null the there is no check
		 */
		private String skipCondition;
		
		private String expression;

		private List<Bucket4JBandWidth> bandwidths = new ArrayList<>();
		
		/**
		 * SPEL expression to dynamic evaluate filter key 
		 */
		
		public String getExpression() {
			return expression;
		}

		public void setExpression(String expression) {
			this.expression = expression;
		}

		public String getSkipCondition() {
			return skipCondition;
		}

		public void setSkipCondition(String skipCondition) {
			this.skipCondition = skipCondition;
		}
		
		public List<Bucket4JBandWidth> getBandwidths() {
			return bandwidths;
		}

		public void setBandwidths(List<Bucket4JBandWidth> bandwidths) {
			this.bandwidths = bandwidths;
		}
		
		public FilterKeyType getFilterKeyType() {
			return filterKeyType;
		}

		public void setFilterKeyType(FilterKeyType filterKeyType) {
			this.filterKeyType = filterKeyType;
		}
		
	}

}
